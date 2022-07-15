import java.rmi.Naming;
import java.util.Scanner;
import java.util.List;
import java.rmi.RemoteException;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileOutputStream;

public class Client {
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] argv) throws Exception {
        Repository r = (Repository) Naming.lookup("rmi://localhost:6231/REPOSITORY");

        boolean running = true;

        while (running)
        {
            System.out.println("\n1. List files");
            System.out.println("2. Upload file");
            System.out.println("3. Delete file");
            System.out.println("4. Download file");
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
                case "3":
                    handleDeleteFile(r);
                    break;
                case "4":
                    handleDownloadFile(r);
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

    private static void handleDeleteFile(Repository r) {
        System.out.print("File URL: ");
        String fileURL = input.next();

        try {
            System.out.print("Deleting file... ");
            r.delete(fileURL);
            System.out.println("SUCCESS");
        } catch (Exception e) {
            System.out.println("FAILED");
            printError("Failed to delete - " + e.getMessage());
        }
    }

    private static void handleDownloadFile(Repository r) {
        System.out.print("File URL: ");
        String fileURL = input.next();

        try {
            System.out.print("Downloading file... ");
            byte[] data = r.download(fileURL);
            System.out.println("SUCCESS");

            String filename = Paths.get(fileURL).getFileName().toString();
            saveFile(data, filename);
        } catch (Exception e) {
            System.out.println("FAILED");
            printError("Failed to download - " + e.getMessage());
        }
    }

    private static void saveFile(byte[] data, String filename) {
        File file = new File(filename);

        if (file.exists()) {
            // Change the filename and try again
            // Splits filename to base and extension, returns array of size 1
            // if extension does not exist, otherwise size 2
            String[] tokens = filename.split("\\.(?=[^\\.]+$)");
            String newFileName = tokens[0] + " - copy";
            if (tokens.length == 2)
                newFileName += "." + tokens[1];

            saveFile(data, newFileName);
        } else {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(data);
            } catch (Exception e) {
                printError("Failed to write: " + e.getMessage());
            }
        }
    }

    private static void printError(String errorMessage) {
        System.out.println("ERROR: " + errorMessage);
    }
}
