import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    DatagramSocket c;

    void run() throws IOException {
        c = new DatagramSocket();
        c.setBroadcast(true);
       
        byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
       
        //Try the 255.255.255.255 first
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
        c.send(sendPacket);
        System.out.println(getClass().getName() + " >>> Request packet sent to: 255.255.255.255 (DEFAULT)");

        //Wait for a response
        byte[] recvBuf = new byte[15000];
        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
        c.receive(receivePacket);
        
        //We have a response
        String hostAddress = receivePacket.getAddress().getHostAddress();
        String msg = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

        System.out.println(getClass().getName() + " >>> Broadcast response from server: " + hostAddress + " msg: " + msg);

        Pattern r = Pattern.compile("ALIVE\s+(\\w+)\s+(\\d+)");
        Matcher m = r.matcher(msg);

        // TODO: Handle the case where the message is malformed
        if (m.find()) {
            int port = Integer.parseInt(m.group(2));

            System.out.println(String.format("%s >>> Identified server %s %d", getClass().getName(), hostAddress, port));

            TCPClient tcpClient = new TCPClient(hostAddress, port);

            String response = tcpClient.recvln();

            System.out.println(getClass().getName() + " >>> TCP response from server: " + response);
        }
    }

    private static class TCPClient {
        private final Scanner scanner;
        private final PrintWriter writer;


        public TCPClient(String address, int port) {
            try {
                Socket s = new Socket(address, port);
                this.scanner = new Scanner(s.getInputStream());
                this.writer = new PrintWriter(s.getOutputStream());
            }
            catch (Exception ex) {
                throw new RuntimeException("Socket I/O Error", ex);
            }
        }

        protected void sendln(String data) {
            writer.println(data); writer.flush();
        }
        protected String recvln() {
            return scanner.nextLine();
        }

        public String runCommand(String command) {
            sendln(command);
            return recvln();
        }
    }
}