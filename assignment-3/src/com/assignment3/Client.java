import java.rmi.Naming;
import java.util.Scanner;
import java.util.List;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] argv) throws Exception {
        Repository r = (Repository) Naming.lookup("rmi://localhost:6231/REPOSITORY");

        // String filename = "test222.txt";
        // byte[] data = "testing".getBytes();
        // int filesize = data.length;
        // r.upload(filename, filesize, data);
        // for (String name: r.listFiles()) {
        //     System.out.println(name);
        // }
        boolean running = true;
        Scanner input = new Scanner(System.in);

        while (running)
        {
            System.out.println("1. List files");
            System.out.println("2. Upload file");
            System.out.println("Input 'q' to exit.");
            String in = input.next();
            switch (in) {
                case "1":
                    handleListFiles(r);
                    break;
                case "2":
                    handleUploadFile(r);
                    break;
                case "q":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }
    }

    private static void handleListFiles(Repository r) throws RemoteException {
        List<String> files = r.listFiles();
        System.out.println(String.format("Found %d files.", files.size()));
        for (int i = 0; i < files.size(); i++)
            System.out.println(String.format("%d. %s", i + 1, files.get(i)));

        System.out.println("");
    }

    private static void handleUploadFile(Repository r) {

    }
}