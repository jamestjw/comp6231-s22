/**
 *
 * @author Jigar Borad
 *
 */
package loadbalancer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EchoClient {

    public static void main(String[] args) throws IOException {
        Socket SocketClient = null;
        BufferedReader InputStream = null;
        PrintStream OutputStream = null;
        String mathProb = null;

        try {
            SocketClient = new Socket("127.0.0.1", 6231);
            OutputStream = new PrintStream(SocketClient.getOutputStream());
            InputStream = new BufferedReader(new InputStreamReader(SocketClient.getInputStream()));
            for (int i = 0; i < 5; i++) {
                mathProb = Problem.generateEq();
                OutputStream.println(mathProb);
                System.out.println(InputStream.readLine());
            }
            OutputStream.println("finish.");

        } catch (UnknownHostException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (SocketClient != null) {
                    SocketClient.close();
                }
                if (InputStream != null) {
                    InputStream.close();
                }

                if (OutputStream != null) {
                    OutputStream.close();
                }

            } catch (IOException e) {
            }
        }
        System.out.println("Finish...");
        Scanner scan = new Scanner(System.in);
        try {
            scan.next();
        } catch (NoSuchElementException e) {

        }
    }
}
