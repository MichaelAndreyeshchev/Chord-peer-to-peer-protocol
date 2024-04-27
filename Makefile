all:
	javac *.java
clean:
	rm -f *.class
run: all
	./try.bash