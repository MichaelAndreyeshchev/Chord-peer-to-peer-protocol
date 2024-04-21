import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class NodeImp extends UnicastRemoteObject implements Node {
    private static final Logger logger = Logger.getLogger(NodeImpl.class.getName());
    private FileHandler fh;
    private int ID;
    private int URL;
    private Node predecessor;
    private Node successor;
    private ConcurrentHashMap<String, String> dictionary;
    private Node[] fingerTable;

    private static final Object joinLock = new Object();
    private boolean isLocked = false;

    NodeImp(String URL) throws RemoteException {
        super()
        fh = new FileHandler("Node" + this.id + ".log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.setUseParentHandlers(false);

        this.URL = URL;
        this.ID = FNV1aHash.hash32(url); // Placeholder for hash function
        this.dictionary = new ConcurrentHashMap<>();
        this.fingerTable = new Node[31];
        this.predecessor = null;
        this.successor = this;

        for (int i = 0; i < 31; ++i) {
            fingerTable[i] = this; 
        }
    }

    public String getURL() throws RemoteException {
        return this.URL;
    }

    public int getID() throws RemoteException {
        return ID;
    }

    public String findSuccessor(int ID, boolean traceFlag) throws RemoteException {
        Node node = findPredecessor(ID, traceFlag);
        return node.getSuccessor().getID(); 
    }

    public String findPredecessor(int ID, boolean traceFlag) throws RemoteException {
        Node node = this;
        while (ID <= node.getID() || ID > node.getSuccessor().getID()) {
            node = node.closestPrecedingFinger(ID);
        }
        return Integer.toString(node.getID());
    }

    public String closestPrecedingFinger(int ID) throws RemoteException {
        for (int i = 30; i >= 0; i--) {
            Node finger = fingerTable[i];

            if (finger.getID() > this.ID && finger.getID() < ID) {
                return Integer.toString(finger.getID());
            }
        }
        return Integer.toString(this.ID);
    }

    public String successor() throws RemoteException {
        return Integer.toString(successor.getID());
    }

    public String predecessor() throws RemoteException {
        return Integer.toString(predecessor.getID());
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

    public boolean insert(String word, String definition) throws RemoteException {
        int key = FNV1aHash.hash32(word);
        Node successorNode = findSuccessor(key, false);

        if (successorNode.getID() == this.ID) {
            dictionary.put(word, definition);
            return true;
        } 
        
        else {
            return successorNode.insert(word, definition);
        }
    }

    public String lookup(String word) throws RemoteException {
        int key = FNV1aHash.hash32(word);
        Node successorNode = findSuccessor(key, false);
        if (successorNode.getID() == this.ID) {
            return dictionary.getOrDefault(word, "Not found");
        } 
        
        else {
            return successorNode.lookup(word);
        }
    }

    public String printFingerTable() throws RemoteException {
        StringBuilder sb = new StringBuilder("Finger Table for Node " + this.ID + ":\n");
        
        for (int i = 0; i < fingerTable.length; i++) {
            int start = modulo31Add(this.ID, (1 << i));
            int fingerId = (fingerTable[i] != null) ? fingerTable[i].getID() : -1;
            String fingerUrl = (fingerTable[i] != null) ? fingerTable[i].getURL() : "null";
            sb.append(String.format("Start: %d, Finger: %d, URL: %s\n", start, fingerId, fingerUrl));
        }
        
        logger.info(sb.toString());
        return sb.toString();
    }

    public String printDictionary() throws RemoteException {
        StringBuilder sb = new StringBuilder("Dictionary Contents for Node " + this.ID + ":\n");
        
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        logger.info(sb.toString());
        return sb.toString();
    }

    public void join(Node node) throws RemoteException {
        if (node != null) {
            initFingerTable(existingNode);
            updateOthers();
        } 
        
        else {

            for (int i = 0; i < 31; i++) {
                fingerTable[i] = this; 
            }

            this.predecessor = this; 
        }
    }

    public void initFingerTable(Node existingNode) throws RemoteException {
        fingerTable[0] = existingNode.findSuccessor(modulo31Add(this.ID, 1));
        this.predecessor = fingerTable[0].getPredecessor();
        fingerTable[0].setPredecessor(this);

        for (int i = 0; i < 31; i++) {
            int start = modulo31Add(this.ID, (1 << (i + 1)));

            if (start >= this.ID && start < fingerTable[i].getID()) {
                fingerTable[i + 1] = fingerTable[i];
            } 
            
            else {
                fingerTable[i + 1] = existingNode.findSuccessor(start);
            }
        }
    }

    public void updateOthers() throws RemoteException {
        for (int i = 0; i < 31; i++) {
            int idMinus2PowI = modulo31Add(this.id, -(1 << i));
            Node p = findPredecessor(modulo31Add(idMinus2PowI, 1));
            p.updateFingerTable(this, i);
        }
    }

    public void updateFingerTable(Node s, int i) throws RemoteException {
        int sID = s.getID();

        if (this == s || isInInterval(sID, this.ID, fingerTable[i].getID())) {
            fingerTable[i] = s;
            Node p = this.getPredecessor();
            p.updateFingerTable(s, i);
        }
    }

    public boolean isInInterval(int key, int start, int end) {
        if (start < end) {
            return key > start && key < end;
        } else {
            return key > start || key < end;
        }
    }

    public int modulo31Add(int n, int m) {
        long result = ((long) n + (long) m) & 0x7FFFFFFFL; // long is used to avoid overflow
        return (int) result; // Cast back to int, safely within the range
    }

}
    

