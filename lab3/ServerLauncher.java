import java.io.IOException;
import java.util.Scanner;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        String serverName = "R1";

        if( args.length > 0)
            serverName = args[0];

        new Server(0, serverName).start();

        System.out.println("\nPress hit ENTER if you wish to stop the servers. Note that the service may NOT stop immediately.");
        new Scanner(System.in).nextLine(); // waiting for EOL from the console to terminate
    }
}
