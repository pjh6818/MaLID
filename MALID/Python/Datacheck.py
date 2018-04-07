import sys
import numpy as np

xy=np.loadtxt(sys.argv[1], delimiter=',', dtype=np.float32)

HR=xy[:, [0]]
xyz_data=xy[:, 1:-1]
Label=xy[:, [-1]]

stop=0
walk=0
run=0
bike=0
stair=0
runningmachine=0
pushup=0
situp=0
squat=0
etc=0

#for x in xyz_data:
    #print(x)
for y in Label:
    if y[0]==0:stop+=1
    elif y[0]==1:walk+=1
    elif y[0]==2:run+=1
    elif y[0]==3:bike+=1
    elif y[0]==4:stair+=1
    elif y[0]==5:runningmachine+=1
    elif y[0]==6:pushup+=1
    elif y[0]==7:situp+=1
    elif y[0]==8:squat+=1
    else : etc+=1
print(sys.argv[1],"Data Status")
print("Stop : ", stop)
print("Walk : ", walk)
print("Run : ", run)
print("Bike : ", bike)
print("Stair : ", stair)
print("RunningMachine : ", runningmachine)
print("Pushup : ", pushup)
print("Situp : ", situp)
print("Squat : ", squat)
print("Etc : ", etc)





