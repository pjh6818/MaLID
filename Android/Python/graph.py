import numpy as np
import matplotlib.pyplot as plt

plt.style.use('ggplot')
plt.xlabel('Index')
plt.ylabel('Magnitude')
xy=np.loadtxt('4.txt', delimiter=',', dtype=np.float32)
xyz_data=xy[:, 1:-1]
Label=xy[:, [-1]]
label = np.unique(Label)
for i in label:
    plt.title('Unsorted Accelerometer value histogram : '+str(int(i)))
    xyz=xyz_data[(np.where(Label == i))[0][5]]
    plt.scatter(np.where(xyz), xyz)
    fig = plt.gcf()
    plt.show()
    fig.savefig('Unsort_'+str(i)+'.png')
    plt.title('Sorted Accelerometer value histogram : '+str(int(i)))
    sort_xyz = np.sort(xyz_data[(np.where(Label == i))[0][5]])
    plt.scatter(np.where(xyz), sort_xyz)
    fig = plt.gcf()
    plt.show()
    fig.savefig('Sort_'+str(i)+'.png')