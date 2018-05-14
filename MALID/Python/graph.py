import numpy as np
import matplotlib.pyplot as plt

plt.style.use('ggplot')
plt.xlabel('Accelerometer value')
plt.ylabel('Frequency')
xy=np.loadtxt('4.txt', delimiter=',', dtype=np.float32)
xyz_mu_sigma=np.loadtxt('tensorflow/xyz_mu_sigma', delimiter=',', dtype=np.float32)

train_xyz_mu=xyz_mu_sigma[0, :]
train_xyz_sigma=xyz_mu_sigma[1, :]
xyz_data=xy[:, 1:-7]
Label=xy[:, [-1]]
label = 0;
idx = 0;
label = np.unique(Label)
for i in label:
    plt.title('Unsorted Accelerometer value histogram : '+str(int(i)))
    plt.hist(xyz_data[(np.where(Label == i))[0][5]], bins=50)
    plt.show()
bound = np.arange(-6, 4)
for i in label:
    plt.title('Sorted Accelerometer value histogram : '+str(int(i)))
    sort_xyz = np.sort(xyz_data[(np.where(Label == i))[0][5]])
    xyz=(sort_xyz-train_xyz_mu)/train_xyz_sigma
    plt.hist(xyz, bins=50)
    plt.show()