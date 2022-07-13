# Assignment 3 - COMP 6231

## How to run?
### Server
```bash
export MPJ_HOME=./mpj-v0_44
javac -cp $MPJ_HOME/lib/mpj.jar  FileDoesNotExistException.java Slave.java Master.java InsufficientStorageException.java BrokenFileException.java Repository.java Server.java DuplicateFilenameException.java InvalidURLException.java Client.java AsciiTable.java RMIServer.java && $MPJ_HOME/bin/mpjrun.sh -np 4 Server
```

### Client
```bash
java -cp "$MPJ_HOME/lib/mpj.jar:./" Client.java
```
