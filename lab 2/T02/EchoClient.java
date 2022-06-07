import java.io.*;
import java.net.*;
import java.util.Scanner;
 
/**
 * Simple UDP socket client to send request and wait for responce from server.
 * @author 
 */
public class EchoClient {
 
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: java EchoClient <hostname> <port>");
            return;
        }
 
        String hostname = args[0];
		String message = "";
        int port = Integer.parseInt(args[1]);
 
        try {
            InetAddress address = InetAddress.getByName(hostname);
            DatagramSocket ds = new DatagramSocket();
	    	Scanner in = new Scanner(System.in);
            while (true) {
 				System.out.print("prompt: > ");
				message = in.nextLine();
                if( message.equals("exit")) break;
				System.out.println();
				byte[] buffer = message.getBytes();
                DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
                ds.send(request);
                buffer = new byte[1024];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                ds.receive(response);
                String echo = new String(buffer, 0, response.getLength());
                System.out.println(echo);
				System.out.println();
            }
        } 
		catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } 
    }
}
