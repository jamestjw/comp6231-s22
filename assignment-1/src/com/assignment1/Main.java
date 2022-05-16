package com.assignment1;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static int SERVER_COUNT = 5;
    public static void main(String[] _args) {
        int[] ports = new int[SERVER_COUNT];
        RepositoryServer[] servers = new RepositoryServer[SERVER_COUNT];

        for (int i = 0; i < SERVER_COUNT; i++) {
            try {
                RepositoryServer server = new RepositoryServer(0, String.format("R%d", i + 1));
                int port = server.getPort();
                server.start();

                servers[i] = server;
                ports[i] = port;
            }
            catch (Exception ex) {
                System.out.println("FATAL ERROR: " + ex.getMessage());
            }
        }

        System.out.println(String.format("\nServers are listening on ports %s.", Arrays.toString(ports)));
        System.out.println("\nPress hit ENTER if you wish to stop the servers. Note that the service may NOT stop immediately.");
        new Scanner(System.in).nextLine(); // waiting for EOL from the console to terminate

        for (int i = 0; i < SERVER_COUNT; i++) {
            servers[i].stop();
        }
    }
}