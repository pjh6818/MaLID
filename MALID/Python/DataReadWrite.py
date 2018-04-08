import sys

f1=open(sys.argv[1],"r")
f2=open("stop_walk_run_train.txt","a")
f3=open("stop_walk_run_test.txt","a")

stop_n=0
walk_n=0
run_n=0

#트레인 데이터 : 300개, 테스트 데이터 : 300개 이후 데이터
while True:
    line = f1.readline()
    if not line: break
    if line[len(line)-2]=="0":
        if stop_n<300:f2.write(line)
        elif stop_n>=300:f3.write(line)
        stop_n+=1
    elif line[len(line)-2]=="1":
        if walk_n<300:f2.write(line)
        elif walk_n>=300:f3.write(line)
        walk_n+=1
    elif line[len(line)-2]=="2":
        if run_n<300:f2.write(line)
        elif run_n>=300:f3.write(line)
        run_n+=1
        
f1.close()
f2.close()
f3.close()