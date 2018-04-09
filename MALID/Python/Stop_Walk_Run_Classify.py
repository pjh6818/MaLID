import sys
import os
import numpy as np
import tensorflow as tf

tf.set_random_seed(777)
xy=np.loadtxt('stop_walk_run_train_pjh.txt', delimiter=',', dtype=np.float32)

pre_hr=xy[:, [0]]
pre_xyz_data=xy[:, 1:145]
Label=xy[:, [-1]]
#데이터 전처리
#xyz 데이터 정렬
sort_xyz_data=np.sort(pre_xyz_data,axis=1) 
#xyz, hr 평균 차감 후 표준편차로 나눠줌
xyz_data=sort_xyz_data-np.mean(sort_xyz_data, axis=0)
xyz_data/=np.std(sort_xyz_data, axis=0)
hr=pre_hr-np.mean(pre_hr, axis=0)
hr/=np.std(pre_hr, axis=0)

def model(HR, XYZ):
    W1=tf.get_variable("W1", shape=[3, 3, 1, 16], initializer=tf.contrib.layers.xavier_initializer())
    L1=tf.nn.conv2d(XYZ, W1, strides=[1, 1, 1, 1], padding='SAME')
    L1=tf.nn.relu(L1)
    L1=tf.nn.max_pool(L1, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    
    W2=tf.get_variable("W2", shape=[3, 3, 16, 32], initializer=tf.contrib.layers.xavier_initializer())
    L2=tf.nn.conv2d(L1, W2, strides=[1, 1, 1, 1], padding='SAME')
    L2=tf.nn.relu(L2)
    L2=tf.nn.max_pool(L2, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    
    L2_flatten=tf.reshape(L2, [-1, 3 * 3 * 32])
    L2_HR=tf.concat([L2_flatten, HR], 1)

    W3=tf.get_variable("W3", shape=[3 * 3 * 32 + 1, 128], initializer=tf.contrib.layers.xavier_initializer())
    b3=tf.Variable(tf.random_normal([128]))
    L3=tf.nn.relu(tf.matmul(L2_HR, W3) + b3)
    
    W4=tf.get_variable("W4", shape=[128, 3], initializer=tf.contrib.layers.xavier_initializer())
    b4=tf.Variable(tf.random_normal([3]))
    L4=tf.matmul(L3, W4) + b4
    
    return L4

learning_rate=0.1
training_epochs=50
batch_size=100;
total_batch=int(len(hr) / batch_size)
Remainder=len(hr)%batch_size
XYZ=tf.placeholder(tf.float32, [None, 144])
XYZ_reshape=tf.reshape(XYZ, [-1, 12, 12, 1])
HR=tf.placeholder(tf.float32, [None, 1])
label=tf.placeholder(tf.int32, [None, 1])
Y_one_hot=tf.one_hot(label, 3)
Y_one_hot=tf.reshape(Y_one_hot, [-1, 3])
sess=tf.Session()


hypothesis=model(HR, XYZ_reshape)
cost=tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits_v2(logits=hypothesis, labels=Y_one_hot))
train=tf.train.GradientDescentOptimizer(learning_rate=learning_rate).minimize(cost)

correct_prediction = tf.equal(tf.argmax(hypothesis, 1), tf.argmax(Y_one_hot, 1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
saver=tf.train.Saver()

checkpoint_dir='model'
model_name='stop_walk_run'
path=os.path.join(checkpoint_dir, model_name)

#트레인 세션
with tf.Session() as sess:
    checkpoint=tf.train.get_checkpoint_state(path)
    
    #Load model
    if checkpoint and checkpoint.model_checkpoint_path:
        checkpoint_name=os.path.basename(checkpoint.model_checkpoint_path)
        saver.restore(sess, os.path.join(path, checkpoint_name))
    else:
        #if pretrained model doesn't exist, train model
        sess.run(tf.global_variables_initializer())
        print('Learning started.')
        for epoch in range(training_epochs):
            avg_cost=0
            for i in range(total_batch):
                idx=i*batch_size
                batch_xyz=xyz_data[idx:idx+batch_size-1,:]
                batch_hr=hr[idx:idx+batch_size-1,:]
                batch_y=Label[idx:idx+batch_size-1,:]
            #if(i==total_batch):
             #   batch_xyz=xyz_data[idx:idx+Remainder-1,:]
              #  batch_hr=hr[idx:idx+Remainder-1,:]
               # batch_y=Label[idx:idx+Remainder-1,:]
                c, _ = sess.run([cost, train], feed_dict={XYZ:batch_xyz, HR:batch_hr, label:batch_y})
                avg_cost += c / total_batch
            print('Epoch:','%04d' % (epoch+1), 'cost=', '{:.9f}'.format(avg_cost))
        print('Learning Finished!')
        if not os.path.exists(path):
            os.makedirs(path)
            saver.save(sess, os.path.join(path, model_name), global_step=0)
    avg_acc=0
    for i in range(total_batch):
        idx=i*batch_size
        batch_xyz=xyz_data[idx:idx+batch_size-1,:]
        batch_hr=hr[idx:idx+batch_size-1,:]
        batch_y=Label[idx:idx+batch_size-1,:]
        acc = sess.run(accuracy, feed_dict={XYZ:batch_xyz, HR:batch_hr, label:batch_y})
        avg_acc += acc / total_batch
    print('accuracy for training data : ', '{:.9f}'.format(avg_acc))

#테스트 세션
with tf.Session() as sess:
    checkpoint=tf.train.get_checkpoint_state(path)
    
    #Load model
    if checkpoint and checkpoint.model_checkpoint_path:
        checkpoint_name=os.path.basename(checkpoint.model_checkpoint_path)
        saver.restore(sess, os.path.join(path, checkpoint_name))
    xy=np.loadtxt('stop_walk_run_test_pjh.txt', delimiter=',', dtype=np.float32)
    xyz_data=xy[:,1:145]
    xyz_data=np.sort(xyz_data, axis=1)
    hr=xy[:,[0]]
    Label=xy[:,[-1]]
    xyz_data=xyz_data-np.mean(sort_xyz_data, axis=0)
    xyz_data/=np.std(sort_xyz_data, axis=0)
    hr=hr-np.mean(pre_hr, axis=0)
    hr/=np.std(pre_hr, axis=0)
    acc = sess.run(accuracy, feed_dict={XYZ:xyz_data, HR:hr, label:Label})
    print('accuracy for test data : ', acc)
    #print("Label: ", Label)
    #print("Prediction: ", sess.run(tf.argmax(hypothesis, 1), feed_dict={XYZ:xyz_data, HR:hr}))