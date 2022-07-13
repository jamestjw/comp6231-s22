import java.rmi.Naming;
import java.util.Scanner;
import java.util.List;
import java.rmi.RemoteException;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

public class Client {
    private static Scanner input = new Scanner(System.in);

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

        while (running)
        {
            System.out.println("\n1. List files");
            System.out.println("2. Upload file");
            System.out.println("Input 'q' to exit.");
            System.out.print("Your next command: ");
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
        System.out.print("Path to file: ");
        String filePath = input.next();
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        File file = new File(filePath);

        if (!file.exists()) {
            printError("File does not exist.");
            return;
        }

        try {
            byte[] data = Files.readAllBytes(path);
            int fileSize = data.length;

            System.out.print(String.format("Uploading %s (%d bytes)...", fileName, fileSize));

            try {
                r.upload(fileName, fileSize, data);
                System.out.println("DONE");
            } catch (Exception e) {
                System.out.println("FAILED");
                printError("Failed to upload file - " + e.getMessage());
            }
        } catch (IOException e) {
            printError("Unable to read from file.");
        }
    }

    private static void printError(String errorMessage) {
        System.out.println("ERROR: " + errorMessage);
    }
}
