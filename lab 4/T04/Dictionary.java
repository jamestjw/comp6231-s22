
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
/**
 * An interface for the remote object: Dictionary.java.
 * @author Pouria Roostaei
 */
public interface Dictionary extends Remote {
    public Map<String, Integer> word(String line) throws RemoteException;

    public Map<String, Integer> word(String[] strings) throws RemoteException;
}