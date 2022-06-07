import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * A server that will run the remote object: Server.java
 * @author Pouria Roostaei
 */

public class Server {

    public static void main(String[] args) {

        try {
            Dictionary server = new DictionaryImpl();
            Dictionary stub = (Dictionary) UnicastRemoteObject.exportObject((Dictionary) server, 0);
            Registry registry = LocateRegistry.createRegistry(6231);
            registry.rebind("Dictionary", stub);

            System.out.println("Server is ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}