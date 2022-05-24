import java.io.IOException;
import java.util.Scanner;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        new Server(0, "R1").start();

        System.out.println("\nPress hit ENTER if you wish to stop the servers. Note that the service may NOT stop immediately.");
        new Scanner(System.in).nextLine(); // waiting for EOL from the console to terminate
    }
}
