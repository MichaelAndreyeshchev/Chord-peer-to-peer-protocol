import java.util.Scanner;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Start the program in the form: java Client <nodeURL>");
            return;
        }

        try {
            String URL = args[0];
            Registry registry = LocateRegistry.getRegistry();
            Node node = (Node) registry.lookup(URL);
        
            
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
                            boolean success = node.insert(insertWord, meaning);
                            if (success) {
                                System.out.println("Result status: Inserted word " + insertWord + " at node " + URL);
                            } 
                            
                            else {
                                System.out.println("Error: Insertion failed.");
                            }

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
