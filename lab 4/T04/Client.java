import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

/**
 * A client to access the server: Client.java
 * @author Pouria Roostaei
 */

public class Client {
    public static void main(String[] args) {

        String host = "localhost";
        Integer port = 6231;
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            Dictionary server = (Dictionary) registry.lookup("Dictionary");

            String line  = "I am student at Concordia University. This is my favourite course at Concordia University.";

            /**
             * Create res variable to receive response from stub.
             * @param res Map<String, Integer>
             */
            Map<String, Integer> res = server.word(line);
            System.out.println("response (String line): " + res);

            String[] words = line.split("\\s+");
            Map<String, Integer> res2 = server.word(words);

            System.out.println("response (String[] lines): " + res2);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}