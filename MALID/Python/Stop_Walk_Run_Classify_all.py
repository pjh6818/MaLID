
# coding: utf-8

# In[2]:


import sys
import os
import numpy as np
import tensorflow as tf

os.environ['TF_CPP_MIN_LOG_LEVEL']='2'
tf.set_random_seed(777)

train_data=np.loadtxt('stop_walk_run_train', delimiter=',', dtype=np.float32)
test_data=np.loadtxt('stop_walk_run_test', delimiter=',', dtype=np.float32)

pre_hr_train=train_data[:, [0]]
pre_xyz_data_train=train_data[:, 1:145]
Label_train=train_data[:, [-1]]

pre_hr_test=test_data[:, [0]]
pre_xyz_data_test=test_data[:, 1:145]
Label_test=test_data[:, [-1]]

#데이터 전처리
#xyz 데이터 정렬
sort_xyz_data_train=np.sort(pre_xyz_data_train, axis=1) 
#xyz, hr 평균 차감 후 표준편차로 나눠줌
xyz_data_train=sort_xyz_data_train-np.mean(sort_xyz_data_train, axis=0)
xyz_data_train/=np.std(sort_xyz_data_train, axis=0)
hr_train=pre_hr_train-np.mean(pre_hr_train, axis=0)
hr_train/=np.std(pre_hr_train, axis=0)

sort_xyz_data_test=np.sort(pre_xyz_data_test, axis=1) 
xyz_data_test=sort_xyz_data_test-np.mean(sort_xyz_data_train, axis=0)
xyz_data_test/=np.std(sort_xyz_data_train, axis=0)
hr_test=pre_hr_test-np.mean(pre_hr_train, axis=0)
hr_test/=np.std(pre_hr_train, axis=0)


# In[3]:


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

    W3=tf.get_variable("W3", shape=[3 * 3 * 8 + 1, 4], initializer=tf.contrib.layers.xavier_initializer())
    b3=tf.Variable(tf.random_normal([4]))
    L3=tf.nn.relu(tf.matmul(L2_HR, W3) + b3)
    
    W4=tf.get_variable("W4", shape=[4, 3], initializer=tf.contrib.layers.xavier_initializer())
    b4=tf.Variable(tf.random_normal([3]))
    L4=tf.matmul(L3, W4) + b4
    
    return L4

learning_rate=0.1
training_epochs=30
batch_size=100;
total_batch=int(len(hr_train) / batch_size)
Remainder=len(hr_train)%batch_size
XYZ=tf.placeholder(tf.float32, [None, 144])
XYZ_reshape=tf.reshape(XYZ, [-1, 12, 12, 1])
HR=tf.placeholder(tf.float32, [None, 1])
label=tf.placeholder(tf.int32, [None, 1])
Y_one_hot=tf.one_hot(label, 3)
Y_one_hot=tf.reshape(Y_one_hot, [-1, 3])


# In[4]:


hypothesis=model(HR, XYZ_reshape)
cost=tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits_v2(logits=hypothesis, labels=Y_one_hot))
train=tf.train.GradientDescentOptimizer(learning_rate=learning_rate).minimize(cost)

correct_prediction = tf.equal(tf.argmax(hypothesis, 1), tf.argmax(Y_one_hot, 1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
saver=tf.train.Saver()

checkpoint_dir='model'
model_name='stop_walk_run_all'
path=os.path.join(checkpoint_dir, model_name)
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
                batch_xyz=xyz_data_train[idx:idx+batch_size-1,:]
                batch_hr=hr_train[idx:idx+batch_size-1,:]
                batch_y=Label_train[idx:idx+batch_size-1,:]
                #if(i==total_batch):
                #    batch_xyz=xyz_data[idx:idx+Remainder-1,:]
                #    batch_hr=hr[idx:idx+Remainder-1,:]
                #    batch_y=Label[idx:idx+Remainder-1,:]
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
        batch_xyz=xyz_data_train[idx:idx+batch_size-1,:]
        batch_hr=hr_train[idx:idx+batch_size-1,:]
        batch_y=Label_train[idx:idx+batch_size-1,:]
        acc = sess.run(accuracy, feed_dict={XYZ:batch_xyz, HR:batch_hr, label:batch_y})
        avg_acc += acc / total_batch
    print('accuracy for training data : ', '{:.9f}'.format(avg_acc))


# In[9]:


with tf.Session() as sess:
    checkpoint=tf.train.get_checkpoint_state(path)
    
    #Load model
    if checkpoint and checkpoint.model_checkpoint_path:
        checkpoint_name=os.path.basename(checkpoint.model_checkpoint_path)
        saver.restore(sess, os.path.join(path, checkpoint_name))
        
    acc = sess.run(accuracy, feed_dict={XYZ:xyz_data_test, HR:hr_test, label:Label_test})
    print('accuracy for test data : ', acc)
    print("Label: ", len(Label_test[:,0]))
    print("Prediction: ", sess.run(tf.argmax(hypothesis, 1), feed_dict={XYZ:xyz_data_test, HR:hr_test}))
# In[ ]:




