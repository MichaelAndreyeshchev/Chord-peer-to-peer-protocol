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

public class NodeImp implements Node {
    private int ID; // node key value
    private String URL; // URL name for the node
    private Node predecessor; // predecessor of node
    //private Node successor;
    private ConcurrentHashMap<String, String> dictionary; // key value pairs at the node
    private Node[] fingerTable; // fingers (represented as nodes) for this node

    private static final Object joinLock = new Object();
    private boolean isLocked = false;

    NodeImp(String URL) throws RemoteException, IOException, NotBoundException {
        super();
        this.URL = URL;
        this.ID = FNV1aHash.hash32(URL); 
        this.dictionary = new ConcurrentHashMap<>();
        this.predecessor = null;
        //this.successor = null;
        fingerTable = new Node[31];
           
    }

    public String getURL() throws RemoteException { // node URL getter
        return this.URL;
    }

    public int getID() throws RemoteException { // node ID getter
        return ID;
    }

    public Node findSuccessor(int id, boolean traceFlag) throws RemoteException { //  During insertion or lookup into the DHT, the client contacts any Chord node for getting the URL  of the node where it would insert or lookup the word corresponding to the key.
        Node n_prime = findPredecessor(id, traceFlag);
        return n_prime.successor(); 
    }

    public Node findPredecessor(int id, boolean traceFlag) throws RemoteException { //  Find the node which precedes the given key (i.e. it is positioned first node going anticlockwiseon the identifier ring from the given key position)
        Node n_prime = this;

        int start = n_prime.getID();
        int end = n_prime.successor().getID();
        while (!isInIntervalEndInclusive(id, start, end)){
            n_prime = n_prime.closestPrecedingFinger(id);
            start = n_prime.getID();
            end = n_prime.successor().getID();
        }

        return n_prime;
    }

    public Node closestPrecedingFinger(int id) throws RemoteException { // closestPrecedingFinger function, as specified in the Chord paper
        for (int i = 30; i >= 0; i--) {
            Node finger_i_node = fingerTable[i];
            if (isInIntervalExclusive(finger_i_node.getID(), this.ID, id)) {
                return finger_i_node;
            }
        }

        return this;
    }

    public Node successor() throws RemoteException { // sucessor getter
        return fingerTable[0];
    }

    public Node predecessor() throws RemoteException { // predecessor getter
        return predecessor;
    }

    public boolean acquireJoinLock(String nodeURL) throws RemoteException { // When a node wants to join the DHT, it will invoke this method at node-0, providing its URL as a parameter.  The node-0 will then return true as response, granting it a lock to proceed with the join protocol. 
        synchronized (joinLock) {
            if (!isLocked) {
                isLocked = true;
                System.out.println("Lock acquired for node: " + nodeURL);
                return true;
            }

            return false;
        }
    }

    public boolean releaseJoinLock(String nodeURL) throws RemoteException { // After the node has joined the DHT, it should notify the node-0 by calling this method. Only after getting this lock-release call, node-0 would then grant lock to some other node to join the DHT. This will prevent Chord nodes from getting added concurrently. 
        synchronized (joinLock) {
            if (isLocked) {
                isLocked = false;
                System.out.println("Lock released for node: " + nodeURL);
                return true;
            }

            return false;
        }
    }

    public void setDictionary(String word, String definition) throws RemoteException { // inserting word definition pairs into the node dictionary
        System.out.println("Inserting word: \"" + word + "\" (key = " + FNV1aHash.hash32(word) + ") at " + this.getURL() + " (key = " + this.getID() + ")");
        dictionary.put(word, definition);
    }

    public ConcurrentHashMap<String, String> getDictionary() { // dictionary getter
        return dictionary;
    }

    public Node insert(String word, String definition) throws RemoteException { // The client can insert key and value at the node whose URL is returned by the findSuccessor(key) call. 
        int key = FNV1aHash.hash32(word);
        if (key == this.ID) {
            this.setDictionary(word, definition);
            return this;
        } 

        Node successorNode = findSuccessor(key, false);
        successorNode.setDictionary(word, definition);
        return successorNode;
    }

    public String lookup(String word) throws RemoteException { //  The client can get the definition for the word from the node whose URL is returned by the find node(key) call. 
        int key = FNV1aHash.hash32(word);
        System.out.println("Looking up word: " + word + " with key: " + key);
        
        if (key == this.ID) {
            System.out.println(dictionary.getOrDefault(word, "Not found"));
            return dictionary.getOrDefault(word, "Not found");
        } 

        Node successorNode = findSuccessor(key, false);
        
        return successorNode.getDictionary().getOrDefault(word, "Not found");
    }

    public String printFingerTable() throws RemoteException { // It will print the finger table of the node to the node’s local logfile called "node-ID.log".
        StringBuilder sb = new StringBuilder("Finger Table for Node " + this.ID + " ("+ this.URL + ") | " + "Predecessor: " + this.predecessor().getURL() + " | Successor: " + this.successor().getURL() + ":\n");

        for (int i = 0; i < fingerTable.length; i++) {
            int start = modulo31Add(this.ID, (1 << i));
            int fingerID = fingerTable[i].getID();
            String fingerURL = fingerTable[i].getURL();

            sb.append(String.format("Start Key: %d | Finger's Node Key: %d | Finger's Node URL: %s\n", start, fingerID, fingerURL));
        }
        
        ChordLogger.log(this.URL, sb.toString() + "\n");
        return sb.toString();
    }

    public String printDictionary() throws RemoteException { //  It will print the contents of the local dictionary contents to the node’s local logfile. 
        StringBuilder sb = new StringBuilder("Total Word Count for " + this.URL + " = " + dictionary.size() + " | Dictionary Contents for " + this.URL + ":\n");
        
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }

        ChordLogger.log(this.URL, sb.toString());
        return sb.toString();
    }

    public void join(Node n_prime) throws RemoteException { // function for allowing a node to join the Chord ring
        if (n_prime != null) {
            initFingerTable(n_prime);
            updateOthers();
        } 
        
        else {
            for (int i = 0; i < 31; i++) {
                fingerTable[i] = this; 
            }

            this.predecessor = this; 
        }

        System.out.println();
        System.out.println(this.getURL() + " Successor = " + this.successor().getURL() + " | " + this.getURL() + " Predecessor = " + this.predecessor().getURL());
        

        String j = predecessor().getURL();
        Node node_j = this.predecessor;
        while (!j.equals(this.getURL())) {
            node_j.printFingerTable();
            System.out.println(node_j.getURL() + " Successor = " + node_j.successor().getURL() + " | " + node_j.getURL() + " Predecessor = " + node_j.predecessor().getURL());
            node_j = node_j.predecessor();
            j = node_j.getURL();
        }
        System.out.println();
        this.printFingerTable();
    }

    public void setPredecessor(Node node) throws RemoteException { // set predecessor variable
        this.predecessor = node;
    }

    public void setSuccessor(Node node) throws RemoteException { // set successor variable
        fingerTable[0] = node;
    }

    public void initFingerTable(Node n_prime) throws RemoteException { // finger table initialization for joining node
        System.out.println("Initializing finger table...");
        this.fingerTable[0] = n_prime.findSuccessor(modulo31Add(this.ID, 1), false);
        this.predecessor = this.fingerTable[0].predecessor();

        this.fingerTable[0].setPredecessor(this);

        for (int i = 0; i < 30; i++) {
            int finger_i_start = modulo31Add(this.ID, (1 << (i + 1)));
            
            if (isInIntervalEndInclusive(finger_i_start, this.ID, fingerTable[i].getID())) {
                fingerTable[i + 1] = fingerTable[i];
            } 
            else {
                fingerTable[i + 1] = n_prime.findSuccessor(finger_i_start, false);
            }
        }
        System.out.println("Finished initializing finger table...");
    }

    public void updateOthers() throws RemoteException { // updaring other finger tables for other nodes
        System.out.println("Updating other nodes...");
        for (int i = 0; i < 31; i++) {
            int idMinus2PowI = modulo31Add(this.ID, -(1 << i) + 1);
            Node p = findPredecessor(idMinus2PowI, false);
            p.updateFingerTable(this, i);
        }
    }

    public void updateFingerTable(Node s, int i) throws RemoteException { // updaring other finger tables for other nodes
        int sID = s.getID();

        int start = modulo31Add(this.ID, (1 << i));
        int end = fingerTable[i].getID();
        if (isInIntervalStartInclusive(sID, start, end)) {
            fingerTable[i] = s;
            Node p = this.predecessor();
            p.updateFingerTable(s, i);
        }
    }

    public boolean isInIntervalStartInclusive(int id, int start, int end) { // [start, end) interval
        if (start < end) {
            return id >= start && id < end;
        } 
        else {
            return id >= start || id < end;
        }
    }

    public boolean isInIntervalEndInclusive(int id, int start, int end) { // (start, end] interval
        if (start < end) {
            return id > start && id <= end;
        } 
        else {
            return id > start || id <= end;
        }
    }

    public boolean isInIntervalExclusive(int id, int start, int end) { // (start, end) interval
        if (start < end) {
            return id > start && id < end;
        } 
        else {
            return id > start || id < end;
        }
    }

    public int modulo31Add(int n, int m) { // add two positive integers in Java in modulo 2^31 arithmetic
        int result = (n + m) & Integer.MAX_VALUE; 
        return result; 
    }

    public static void main(String[] args) throws RemoteException, IOException, NotBoundException {
        if (args.length != 3) {
            System.out.println("Usage: java NodeImp <node_URL_ID> <node_IP_Address> <node_port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[2]);
        String hostname = args[1];
        String name = "node-" + args[0];
        NodeImp node;
        Registry registry;

        try {
            registry = LocateRegistry.createRegistry(port);
        }

        catch (Exception e) {
            registry = LocateRegistry.getRegistry(port);
        }

        node = new NodeImp(name);
        System.setProperty("java.rmi.server.hostname", hostname);
        Node nodeStub = (Node) UnicastRemoteObject.exportObject(node, 0);

        try {
            registry.bind(name, nodeStub);
            System.out.println("Node " + name + " is running...");
        } 
        catch (Exception e) {
            registry.rebind(name, nodeStub);
            System.out.println("Node " + name + " is running...");
        }

        if (!node.URL.equals("node-0")){
            System.out.println("Joining node-0...");
            Node node0 = (Node) LocateRegistry.getRegistry(hostname, port).lookup("node-0");

            while(!node0.acquireJoinLock(name));
            node.join(node0);
            System.out.println("Node " + name + " joined node-0");
            node0.releaseJoinLock(name);
        }

        else{
            System.out.println("Empty Chord ring...");
            node.join(null);
        }
    }
}
    

