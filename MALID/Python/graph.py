import numpy as np
import matplotlib.pyplot as plt

plt.interactive(False)
plt.style.use('ggplot')
plt.title('Accelerometer value histogram')
plt.xlabel('Accelerometer value')
plt.ylabel('Frequency')
xy=np.loadtxt('4.txt', delimiter=',', dtype=np.float32)

HR=xy[:, [0]]
xyz_data=xy[:, 1:-1]
Label=xy[:, [-1]]
label = 0;
idx = 0;
label = np.unique(Label)
for i in label:
    plt.hist(xyz_data[np.where(Label == i)[0]])

