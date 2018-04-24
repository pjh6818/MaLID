import sys
import numpy as np

xy=np.loadtxt(sys.argv[1], delimiter=',', dtype=np.float32)

HR=xy[:, [0]]
xyz_data=xy[:, 1:-1]
Label=xy[:, [-1]]

stop=0
walk=0
run=0
walk_hand=0
run_hand=0
bike=0
etc=0
#for x in xyz_data:
    #print(x)
for idx,y in enumerate(Label):
    if y[0]==0:stop+=1
    elif y[0]==1:walk+=1
    elif y[0]==2:run+=1
    elif y[0]==3:walk_hand+=1
    elif y[0]==4:run_hand+=1
    elif y[0]==5:bike+=1
    else:etc+=1
print(sys.argv[1],"Data Status")
print("Stop : ", stop)
print("Walk : ", walk)
print("Run : ", run)
print("Walk_hand : ", walk_hand)
print("Run_hand : ", run_hand)
print("Bike : ", bike)
print("Etc : ", etc)
