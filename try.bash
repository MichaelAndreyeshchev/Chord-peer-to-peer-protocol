#!/bin/bash
java NodeImp 0 127.0.0.1 1099&
sleep 1
java NodeImp 1 127.0.0.1 1099&
java NodeImp 2 127.0.0.1 1099&
java NodeImp 3 127.0.0.1 1099&
java NodeImp 4 127.0.0.1 1099&
java NodeImp 5 127.0.0.1 1099&
java NodeImp 6 127.0.0.1 1099&
java NodeImp 7 127.0.0.1 1099&