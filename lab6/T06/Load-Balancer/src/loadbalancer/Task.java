/**
 *
 * @author Jigar Borad
 *
 */
package loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Task implements Runnable {

    Socket clientSocket;
    int portServer;

    public Task(Socket clientSocket, int port) {
        this.clientSocket = clientSocket;
        this.portServer = port;
    }

    public void run() {

        System.out.println("Task has started");

        try {
            /* Buffers of client */
            BufferedReader inClient = null;
            PrintStream outClient = null;
            inClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outClient = new PrintStream(clientSocket.getOutputStream());

            Socket serverSocket = new Socket("localhost", portServer);
            Scanner serverScanner = new Scanner(serverSocket.getInputStream());
            PrintWriter serverWriter = new PrintWriter(serverSocket.getOutputStream());

            while (true) {
                String userInput = inClient.readLine();
                serverWriter.println(userInput);
                serverWriter.flush();
                String serverResponse;
                try {
                    serverResponse = serverScanner.nextLine();
                } catch (NoSuchElementException e) {
                    break;
                }
                outClient.println(serverResponse);
                outClient.flush();
            }

        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
