Group members: Michael and Wyatt (andr0821 & rasmu984)

# Status Disclosure:
This program has been checked for bugs and should not have any errors in any part of the system. 

# IMPORTANT NOTE:
Note that below "node_URL_ID" is an integer greater than or equal to 0, "nodeURL" are named as "node-ID" where ID is an integer greater than or equal to 0. NOTE THAT THE node-ID.log files need to be deleted if you want to have a fresh copy of the logs before you start up the Chord nodes. 

# File Details
* ChordLogger.java contains the event logging containing the finger table information for each node as well as the word definition pair dictionary information.

* Client.java contains the client designed for interactive insertions and lookups.

* DictionaryLoader.java contains the code to load into your Chord system the dictionary data in the 
given file

* FNV1aHash.java contains the hashing information to get the hash value of a key.

* node-ID.log where ID is greater than or equal to 0 contains the finger table and word definitions/count logs for each node 

* Node.java is the interface for NodeImp.java

* NodeImp.java contains the central code for the Chord system

* try.bash is the script responsible for getting 8 Chord nodes running

# Running a Specific Node with Interative Client Program:
```
$ javac *.java

$ java NodeImp <node_URL_ID> <node_IP_Address> <node_port>

$ java Client <nodeHost> <nodePort> <nodeURL>
```
# EXAMPLE -- Running a Specific Node with Interative Client Program:
```
$ javac *.java

$ java NodeImp 0 csel-kh1250-03.cselabs.umn.edu 1098

$ java Client csel-kh1250-03.cselabs.umn.edu 1098 node-0
```
# Running 8-node Chord System with Interative Client Program:
```
$ javac *.java

$ ./try.bash

$ java Client <nodeHost> <nodePort> <nodeURL>
```
# EXAMPLE -- Running 8-node Chord System with Interative Client Program:
```
$ javac *.java

$ ./try.bash

$ java Client csel-kh1250-03.cselabs.umn.edu 1098 node-0
```
# Running 8-node Chord System with Dictionary Loader Program LOCALLY:
Note, for the bash file to work it may be necessary to give execute permissions beforehand using:
```
$ chmod +x try.bash
```
Then, running the code:
```
$ javac *.java

$ ./try.bash

$ java DictionaryLoader <nodeURL> sample-dictionary-file.txt
```
# EXAMPLE -- Running 8-node Chord System with Dictionary Loader Program LOCALLY:
Note, for the bash file to work it may be necessary to give execute permissions beforehand using:
```
$ chmod +x try.bash
```
Then, running the code:
```
$ javac *.java

$ ./try.bash

$ java DictionaryLoader node-0 sample-dictionary-file.txt
```
# Running 8-node Chord System with Dictionary Loader Program REMOTELY:
Note, for the bash file to work it may be necessary to give execute permissions beforehand using:
```
$ chmod +x try.bash
```
Then, running the code:
```
$ javac *.java

$ ./remote.try.bash

$ java DictionaryLoader <nodeURL> sample-dictionary-file.txt
```
# EXAMPLE -- Running 8-node Chord System with Dictionary Loader Program REMOTELY:
Note, for the bash file to work it may be necessary to give execute permissions beforehand using:
```
$ chmod +x try.bash
```
Then, running the code:
```
$ javac *.java

$ ./remote.try.bash

$ java DictionaryLoader node-0 sample-dictionary-file.txt
```
# Compilation using the Makefile:
```
$ make
```
# Cleaning the directory using the Makefile:
```
$ make clean
```
# Running 8 nodes using the Makefile LOCALLY ONLY:
```
$ make run
```

# Logging:
Note that logging happens for every single node in the Chord system. Each of the log files is named in the format "node-ID.log", where the ID is an integer greater than or equal to 0. The file is logged by representing the key value of the node, its predecessor's URL, its successor's URL, and all of the 31 finger contents (start key, finger node's key, and finger node's URL). Everytime a new node joins this information is logged in the "node-ID.log" file. Also, when some node-ID is updated due to some node joining the Chord system, the information described above is relogged again. Thus, the most up to date finger table information for some node-ID can be found on the last block of continuous text describing the finger table at the bottom of "node-ID.log" where ID is some node ID. Note that the word counts and words stored in each of the nodes is stored in node-ID.log. As with the finger table information, the most recently stored word definition pairs are found at the bottom of the file.  

# Text file containing the finger tables of each of the 8 nodes in an 8-node Chord system, associated ring’s diagram, and word counts:
This can be foun in "Chord_Ring.txt", with the actual ring diagram (containing the URL and hash-key value of each Chord node) is found in "Chord_Ring_Topology.png"