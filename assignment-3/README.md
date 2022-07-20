# Assignment 3 - COMP 6231

## How to run?
### Server
```bash
cd src/com/assignment3
# You need to get a copy of mpj and unzip it here
# unzip mpj-v0_44.zip
export MPJ_HOME=./mpj-v0_44
javac -cp $MPJ_HOME/lib/mpj.jar *.java && $MPJ_HOME/bin/mpjrun.sh -np 4 Server
```

### Client
```bash
java Client.java
```
