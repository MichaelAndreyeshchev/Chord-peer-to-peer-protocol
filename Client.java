import java.util.Scanner;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Start the program in the form: java Client <someChordNodeURL>");
            return;
        }

        try {
            String[] urlParts = args[0].split("/+", 3);
            String[] hostDomain = urlParts[1].split(":");
            String hostname = hostDomain[0];
            String port = hostDomain[1];

            Registry registry = LocateRegistry.getRegistry(hostname, Integer.parseInt(port));
            Node node = (Node) registry.lookup(urlParts[2]);
        
            
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Enter 1 to lookup, 2 to insert a new (key-value) item, or 3 to exit");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); 

                switch (choice) {
                    case 1:
                        System.out.println("Enter a word:");
                        String lookupWord = scanner.nextLine();

                        try {
                            String result = node.lookup(lookupWord);
                            System.out.println("Result: " + result);
                        } 
                        
                        catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }

                        break;

                    case 2:
                        System.out.println("Enter a word:");
                        String insertWord = scanner.nextLine();
                        System.out.println("Enter the meaning:");
                        String meaning = scanner.nextLine();

                        try {
                            Node n = node.insert(insertWord, meaning);
                            System.out.println("Result status: Inserted word " + insertWord + " at node " + n.getURL() + " (key = " + FNV1aHash.hash32(insertWord) + ")");
                        } 
                        
                        catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }

                        break;

                    case 3:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;
                        
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
        } 
    
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }
}
