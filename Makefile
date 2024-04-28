all:
	javac *.java
clean:
	rm -f *.class
	rm -f *.log
run: all
	./try.bash