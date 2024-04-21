import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.HashMap;
import java.util.Map;

public class NodeImp extends UnicastRemoteObject implements Node {
    private int ID;
    private String URL;
    private Node predecessor;
    private Node successor;
    private ConcurrentHashMap<String, String> dictionary;
    private Node[] fingerTable;

    private static final Object joinLock = new Object();
    private boolean isLocked = false;

    NodeImp(String URL) throws RemoteException, IOException, NotBoundException {
        super();
        this.URL = URL;
        this.ID = FNV1aHash.hash32(URL); 
        this.dictionary = new ConcurrentHashMap<>();
        this.predecessor = null;
        this.successor = null;
        fingerTable = new Node[5];
           
    }

    public String getURL() throws RemoteException {
        return this.URL;
    }

    public int getID() throws RemoteException {
        return ID;
    }

    public Node findSuccessor(int id, boolean traceFlag) throws RemoteException {
        Node n_prime = findPredecessor(id, traceFlag);
        return n_prime.successor(); 
    }

    public Node findPredecessor(int id, boolean traceFlag) throws RemoteException {
        Node n_prime = this;

        //while (!(ID > node.getID() && ID <= node.successor().getID()) && (node.getID() != node.successor().getID())){
        
        while (!(id > n_prime.getID() && id <= n_prime.successor().getID()) && (n_prime.getID() != n_prime.successor().getID())){
            n_prime = n_prime.closestPrecedingFinger(id);
        }

        return n_prime;
    }

    public Node closestPrecedingFinger(int id) throws RemoteException {
        for (int i = 4; i >= 0; i--) {
            Node finger_i_node = fingerTable[i];
            if (finger_i_node.getID() > this.ID && finger_i_node.getID() < id) {
                return finger_i_node;
            }
        }

        return this;
    }

    public Node successor() throws RemoteException {
        return successor;
    }

    public Node predecessor() throws RemoteException {
        return predecessor;
    }

    public boolean acquireJoinLock(String nodeURL) throws RemoteException {
        synchronized (joinLock) {
            if (!isLocked) {
                isLocked = true;
                System.out.println("Lock acquired for node: " + nodeURL);
                return true;
            }

            return false;
        }
    }

    public boolean releaseJoinLock(String nodeURL) throws RemoteException {
        synchronized (joinLock) {
            if (isLocked) {
                isLocked = false;
                System.out.println("Lock released for node: " + nodeURL);
                return true;
            }

            return false;
        }
    }

    public void setDictionary(String word, String definition) {
        dictionary.put(word, definition);
    }

    public ConcurrentHashMap<String, String> getDictionary() {
        return dictionary;
    }

    public boolean insert(String word, String definition) throws RemoteException {
        int key = FNV1aHash.hash32(word);
        Node successorNode = findSuccessor(key, false);

        if (successorNode.getID() == this.ID) {
            dictionary.put(word, definition);
            return true;
        } 
        
        else {
            successorNode.setDictionary(word, definition);
            return true;
        }
    }

    public String lookup(String word) throws RemoteException {
        int key = FNV1aHash.hash32(word);
        System.out.println("Looking up word: " + word + " with key: " + key);

        Node successorNode = findSuccessor(key, false);

        if (successorNode.getID() == this.ID) {
            System.out.println(dictionary.getOrDefault(word, "Not found"));
            return dictionary.getOrDefault(word, "Not found");
        } 
        
        else {
            return successorNode.getDictionary().getOrDefault(word, "Not found");
        }
    }

    public String printFingerTable() throws RemoteException {
        StringBuilder sb = new StringBuilder("Finger Table for Node " + this.ID + " ("+ this.URL + "):\n");
        
        for (int i = 0; i < fingerTable.length; i++) {
            int start = modulo31Add(this.ID, 1);
            int fingerID = fingerTable[i].getID();
            String fingerURL = fingerTable[i].getURL();

            sb.append(String.format("Start Key: %d | Finger's Node Key: %d | Finger's Node URL: %s\n", start, fingerID, fingerURL));
        }
        
        ChordLogger.log(this.URL, sb.toString());
        return sb.toString();
    }

    public String printDictionary() throws RemoteException {
        StringBuilder sb = new StringBuilder("Dictionary Contents for Node " + this.ID + ":\n");
        
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }

        ChordLogger.log(this.URL, sb.toString());
        return sb.toString();
    }

    public void join(Node n_prime) throws RemoteException {
        if (n_prime != null) {
            initFingerTable(n_prime);
            updateOthers();
        } 
        
        else {
            for (int i = 0; i < 5; i++) {
                fingerTable[i] = this; 
            }

            this.predecessor = this; 
            this.successor = this;
        }
    }

    public void setPredecessor(Node node) throws RemoteException { 
        this.predecessor = node;
    }

    public void setSuccessor(Node node) throws RemoteException { 
        this.successor = node;
    }

    public void initFingerTable(Node n_prime) throws RemoteException {
        System.out.println("Initializing finger table...");
        fingerTable[0] = n_prime.findSuccessor(modulo31Add(this.ID, 1), false);
        this.successor = fingerTable[0];
        this.predecessor = this.successor.predecessor();
        this.successor.setPredecessor(this);

        for (int i = 0; i < 4; i++) {
            System.out.println("loop iteration: " + i);
            int finger_i_start = modulo31Add(this.ID, (1 << (i + 1)));

            if ((finger_i_start > this.ID) && (finger_i_start < fingerTable[i].getID())) {
                fingerTable[i + 1] = fingerTable[i];
            } 
            
            else {
                fingerTable[i + 1] = n_prime.findSuccessor(finger_i_start, false);
            }
        }
        System.out.println("Finished initializing finger table...");
        printFingerTable();
    }

    public void updateOthers() throws RemoteException {
        System.out.println("Updating other nodes...");
        for (int i = 0; i < 5; i++) {
            int idMinus2PowI = this.ID + -(1 << i);
            Node p = findPredecessor(idMinus2PowI + 1, false);
            p.updateFingerTable(this, i);
        }
    }

    public void updateFingerTable(Node s, int i) throws RemoteException {
        int sID = s.getID();

        //if (this == s || isInInterval(sID, this.ID, fingerTable[i].getID())) {
        if ((sID >= modulo31Add(this.ID, (1 << i))) && sID < fingerTable[i].getID()) {
            fingerTable[i] = s;
            Node p = this.predecessor();
            p.updateFingerTable(s, i);
        }
        printFingerTable();
    }

    public int modulo31Add(int n, int m) {
        int result = (n + m) & Integer.MAX_VALUE; 
        return result; 
    }

    public static void main(String[] args) throws RemoteException, IOException, NotBoundException {
        if (args.length != 2) {
            System.out.println("Usage: java NodeImp <nodeURL> <node0IP>");
            System.exit(1);
        }

        String name = "node-" + args[0];
        NodeImp node;
        try {
            node = new NodeImp(name);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind(name, node);
            System.out.println("Node " + name + " is running...");
        } 
        catch (Exception e) {
            node = new NodeImp(name);
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind(name, node);
            System.out.println("Node " + name + " is running...");
        }

        if (!node.URL.equals("node-0")){
            System.out.println("Joining node-0...");
            Node node0 = (Node) LocateRegistry.getRegistry(args[1], 1099).lookup("node-0");
            node.join(node0);
            System.out.println("Node " + name + " joined node-0");
        }

        else{
            System.out.println("Empty Chord ring...");
            node.join(null);
        }
    }
}
    

