import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DictionaryLoader {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please execute as follows: java DictionaryLoader <nodeURL> <dictionaryFile>");
            return;
        }

        String dictionaryFilePath = args[1];
        String[] urlParts = args[0].split("/+", 3);
        String[] hostDomain = urlParts[1].split(":");
        String hostname = hostDomain[0];
        String port = hostDomain[1];

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, Integer.parseInt(port));
            Node node = (Node) registry.lookup(urlParts[2]);

            String line;
            BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilePath));

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" : ", 2);

                if (parts.length == 2) {
                    String word = parts[0].trim();
                    String meaning = parts[1].trim();

                    node.insert(word, meaning);

                } 
                
                else {
                    System.out.println("Skipping malformed line: " + line);
                }
            }

            reader.close();
            System.out.println("Dictionary loaded successfully.");
            
            node.printDictionary();

            Node prevNode = node.predecessor();
            while (prevNode.getID() != node.getID()) {
                prevNode.printDictionary();
                prevNode = prevNode.predecessor();
            }
        } 
        
        catch (Exception e) {
            System.err.println("Exception while loading dictionary: " + e.toString());
            e.printStackTrace();
        }
    }
}
