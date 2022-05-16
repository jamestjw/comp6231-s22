package com.assignment1;

import java.util.Scanner;

public class Main {
    public static void main(String[] _args) {
        try {
            RepositoryServer server = new RepositoryServer(0);
            int port = server.getPort();
            server.start();
            System.out.println(String.format("\nThe server is listening on port %d.", port));
            System.out.println("\nPress hit ENTER if you wish to stop the server. Note that the service may NOT stop immediately.");
            new Scanner(System.in).nextLine(); // waiting for EOL from the console to terminate
            server.stop();
        }
        catch (Exception ex) {
            System.out.println("FATAL ERROR: " + ex.getMessage());
        }
    }
}