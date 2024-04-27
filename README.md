Group members: Michael and Wyatt (andr0821 & rasmu984)

# Running a Specific Node with Interative Client Program:
```
$ javac *.java

$ java NodeImp <node_URL_ID> <node_IP_Address>

$ java Client <nodeURL>
```
# Running 8-node Chord System with Interative Client Program:
```
$ javac *.java

$ ./try.bash

$ java Client <nodeURL>
```
# Running 8-node Chord System with Dictionary Loader Program:
```
$ javac *.java

$ ./try.bash

$ java DictionaryLoader <nodeURL> sample-dictionary-file.txt
```
# Logging:
Note that logging happens for every single node in the Chord system. Each of the log files is named in the format "node-ID.log". The file is logged by representing the key value of the node, its predecessor's URL, its successor's URL, and all of the 31 finger contents (start key, finger node's key, and finger node's URL). Everytime a new node joins this information is logged in the "node-ID.log" file. Also, when some node-ID is updated due to some node joining the Chord system, the information described above is relogged again. Thus, the most up to date information for some node-ID can be found on the last block of continuous text at the bottom of "node-0.log". 