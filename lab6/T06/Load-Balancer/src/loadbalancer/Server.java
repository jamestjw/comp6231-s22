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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    ServerSocket listenSocket = null;

    public Server(int port) {
        try {
            listenSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        execution();

    }

    void execution() {

        while (true) {
            Socket clientSocket = null;
            BufferedReader in = null;
            PrintStream out = null;
            String theLine = null;

            while (clientSocket == null) {
                try {
                    clientSocket = listenSocket.accept();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintStream(clientSocket.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (true) {
                try {
                    theLine = in.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("I received : " + theLine + " of " + clientSocket.getPort());
                if (theLine.equals("finish.")) {
                    break;
                }

                out.println(theLine + " = " + Problem.calculateEq(theLine) + "   " + clientSocket.getPort());
            }
            try {
                clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String[] args) {
        System.out.println("Server connected on the port : " + args[0]);
        new Server(Integer.parseInt(args[0]));
    }

}
