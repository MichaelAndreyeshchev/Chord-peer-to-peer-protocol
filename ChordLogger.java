import java.io.*;

public class ChordLogger {
    public static synchronized void log(String fileName, String output) {
        try (FileWriter fileWriter = new FileWriter(fileName + ".log", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

            printWriter.print(output);
        
        }

        catch (IOException e) {
            System.err.println("Logger failed to log!");
        }
    }
}
