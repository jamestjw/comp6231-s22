import java.io.*;
import java.net.*;
import java.util.*;
 
/**
 * Server using UDP socket to send echo response to a client.
 *
 *
 * @author
 */
public class EchoServer {
    private DatagramSocket socket;
    private HashMap<String, Integer> dataMap = new HashMap<>();

    public EchoServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }
 
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntax: java EchoServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
 
        try {
            EchoServer server = new EchoServer(port);
            server.protocol();
        } 
		catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } 
    }
 
    private void protocol() throws IOException {
		while(true){
		    byte[] buffer = new byte[1024];
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			socket.receive(request);

            InetAddress clientAddress = request.getAddress();
			int clientPort = request.getPort();
            String requestString = new String(request.getData(), request.getOffset(), request.getLength());
            String clientID = getClientIdentifier(clientAddress, clientPort);

            int value;

            try {
                value = Integer.parseInt(requestString);
            } catch (NumberFormatException ex){
                sendMessage("Invalid integer", clientAddress, clientPort);
                continue;
            }

            // Check if user has already provided us with the first value
            if (!dataMap.containsKey(clientID)) {
                handleUserFirstValue(clientID, value, clientAddress, clientPort);
            } else {
                handleUserSecondValue(clientID, value, clientAddress, clientPort);
            }
		}
	}

    private void sendMessage(String msg, InetAddress address, int port) throws IOException {
        byte[] msgBytes = msg.getBytes();
        DatagramPacket response = new DatagramPacket(msgBytes, msgBytes.length, address, port);
        socket.send(response);
    }

    private void handleUserFirstValue(String clientID, Integer value, InetAddress address, int port) throws IOException {
        dataMap.put(clientID, value);
        sendMessage("send me 2nd term", address, port);
    }

    private void handleUserSecondValue(String clientID, Integer value, InetAddress address, int port) throws IOException {
        int firstVal = dataMap.get(clientID);
        Integer res = firstVal + value;
        sendMessage(res.toString(), address, port);
        dataMap.remove(clientID);
    }

    private String getClientIdentifier(InetAddress address, int port) {
        return String.format("%s:%d", address.getHostAddress(), port);
    }
}
