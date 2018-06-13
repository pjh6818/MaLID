import sys
import os
import numpy as np
import tensorflow as tf

os.environ['TF_CPP_MIN_LOG_LEVEL']='2'
tf.set_random_seed(777)

xyz_mu_sigma=np.loadtxt('tensorflow/model/stop_walk_run_hand_armup_all_3/xyz_mu_sigma', delimiter=',', dtype=np.float32)
hr_mu_sigma=np.loadtxt('tensorflow/model/stop_walk_run_hand_armup_all_3/hr_mu_sigma', delimiter=',', dtype=np.float32)

train_xyz_mu=xyz_mu_sigma[0, :]
train_xyz_sigma=xyz_mu_sigma[1, :]
train_hr_mu=hr_mu_sigma[0]
train_hr_sigma=hr_mu_sigma[1]
real_data=np.fromstring(sys.argv[1], dtype=float, sep=',')
real_data=real_data.reshape(1,151)
pre_hr=real_data[:, [0]]
pre_xyz=real_data[:, 1:151]
sort_xyz=np.zeros([1,150])
for i in range(50):
	sort_xyz[:, i]=pre_xyz[:,3*i]
	sort_xyz[:, i+50]=pre_xyz[:,3*i+1]
	sort_xyz[:, i+100]=pre_xyz[:,3*i+2]
sort_xyz[:,0:50]=np.sort(sort_xyz[:, 0:50], axis=1)
sort_xyz[:,50:100]=np.sort(sort_xyz[:, 50:100], axis=1)
sort_xyz[:,100:150]=np.sort(sort_xyz[:, 100:150], axis=1)

sort_xyz=np.absolute(sort_xyz[:, 1:145])
#sort_xyz=np.sort(pre_xyz, axis=1) 
xyz=sort_xyz-train_xyz_mu
xyz/=train_xyz_sigma
hr=pre_hr-train_hr_mu
hr/=train_hr_sigma

def model(HR, XYZ):
    W1=tf.get_variable("W1", shape=[3, 3, 1, 4], initializer=tf.contrib.layers.xavier_initializer())
    L1=tf.nn.conv2d(XYZ, W1, strides=[1, 1, 1, 1], padding='SAME')
    L1=tf.nn.relu(L1)
    L1=tf.nn.max_pool(L1, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    
    W2=tf.get_variable("W2", shape=[3, 3, 4, 8], initializer=tf.contrib.layers.xavier_initializer())
    L2=tf.nn.conv2d(L1, W2, strides=[1, 1, 1, 1], padding='SAME')
    L2=tf.nn.relu(L2)
    L2=tf.nn.max_pool(L2, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    
    L2_flatten=tf.reshape(L2, [-1, 3 * 3 * 8])
    L2_HR=tf.concat([L2_flatten, HR], 1)

    W3=tf.get_variable("W3", shape=[3 * 3 * 8 + 1, 8], initializer=tf.contrib.layers.xavier_initializer())
    b3=tf.Variable(tf.random_normal([8]))
    L3=tf.nn.relu(tf.matmul(L2_HR, W3) + b3)
    
    W4=tf.get_variable("W4", shape=[8, 4], initializer=tf.contrib.layers.xavier_initializer())
    b4=tf.Variable(tf.random_normal([4]))
    L4=tf.matmul(L3, W4) + b4
    
    return L4

learning_rate=0.1
XYZ=tf.placeholder(tf.float32, [None, 144])
XYZ_reshape=tf.reshape(XYZ, [-1, 12, 12, 1])
HR=tf.placeholder(tf.float32, [None, 1])
label=tf.placeholder(tf.int32, [None, 1])
Y_one_hot=tf.one_hot(label, 4)
Y_one_hot=tf.reshape(Y_one_hot, [-1, 4])

hypothesis=model(HR, XYZ_reshape)
cost=tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits_v2(logits=hypothesis, labels=Y_one_hot))
train=tf.train.GradientDescentOptimizer(learning_rate=learning_rate).minimize(cost)

correct_prediction = tf.equal(tf.argmax(hypothesis, 1), tf.argmax(Y_one_hot, 1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
saver=tf.train.Saver()

checkpoint_dir='tensorflow/model'
model_name='stop_walk_run_hand_armup_all_3'
path=os.path.join(checkpoint_dir, model_name)

with tf.Session() as sess:
    checkpoint=tf.train.get_checkpoint_state(path)
    
    #Load model
    if checkpoint and checkpoint.model_checkpoint_path:
        checkpoint_name=os.path.basename(checkpoint.model_checkpoint_path)
        saver.restore(sess, os.path.join(path, checkpoint_name))
	
    print(sess.run(tf.argmax(hypothesis, 1), feed_dict={XYZ:xyz, HR:hr})[0])
       
