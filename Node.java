/* You may use and modify this interface file for your Assignment 7 */
/* You may add new methods or change any of the methods in this interface. */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
  public Node  findSuccessor (int key, boolean traceFlag) throws RemoteException;
  public Node  findPredecessor (int key, boolean traceFlag) throws RemoteException;
  public Node  closestPrecedingFinger (int key) throws RemoteException;
  public Node  successor () throws RemoteException;
  public Node  predecessor  () throws RemoteException;
  public void setPredecessor (Node node) throws RemoteException;
  public boolean acquireJoinLock (String nodeURL) throws RemoteException;
  public boolean releaseJoinLock (String nodeURL) throws RemoteException;
  public boolean insert (String word, String definition) throws RemoteException;
  public String  lookup (String word) throws RemoteException;
  public String  printFingerTable() throws RemoteException;
  public String  printDictionary() throws RemoteException;
  public int getID() throws RemoteException;
  public String getURL() throws RemoteException;
  public void join(Node node) throws RemoteException;
  public void initFingerTable(Node node) throws RemoteException;
  public void updateOthers() throws RemoteException;
  public void updateFingerTable(Node s, int i) throws RemoteException;
  public boolean isInInterval(int key, int start, int end) throws RemoteException;
  public int modulo31Add(int n, int m) throws RemoteException;
}

