import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    DatagramSocket c;
    List<ServerDetails> serverList;

    public Client() throws IOException {
        c = new DatagramSocket();
        c.setBroadcast(true);

        serverList = new ArrayList<>();
    }

    void peerDiscovery() throws IOException {
        byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
       
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
        c.send(sendPacket);
        System.out.println(getClass().getName() + " >>> Request packet sent to: 255.255.255.255 (DEFAULT)");

        //Wait for a response
        byte[] recvBuf = new byte[15000];
        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);


        while (true) {
            c.receive(receivePacket);
        
            // We have a response
            String hostAddress = receivePacket.getAddress().getHostAddress();
            String msg = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
    
            System.out.println(getClass().getName() + " >>> Broadcast response from server: " + hostAddress + " msg: " + msg);
    
            Pattern r = Pattern.compile("ALIVE\\s+(\\w+)\\s+(\\d+)");
            Matcher m = r.matcher(msg);
    
            // TODO: Handle the case where the message is malformed
            if (m.find()) {
                int port = Integer.parseInt(m.group(2));
                String name = m.group(1);
    
                System.out.println(String.format("%s >>> Identified server %s %d", getClass().getName(), hostAddress, port));
    
                serverList.add(new ServerDetails(hostAddress, port, name));
            }
        }
    }

    void run() throws IOException {
        new Thread(() -> {
            try {
                peerDiscovery();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (serverList.size() == 0) {
            System.out.println(getClass().getName() + " >>> NO server found");
        } else {
            System.out.println(String.format("%s >>> %d servers found", getClass().getName(), serverList.size()));

            // Get random server
            Random rand = new Random();
            ServerDetails details = serverList.get(rand.nextInt(serverList.size()));

            System.out.println(String.format("%s >>> Connecting to random server %s", getClass().getName(), details.name));

            TCPClient tcpClient = new TCPClient(details.address, details.port);

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

    private class ServerDetails {
        String address;
        int port;
        String name;
        public ServerDetails(String address, int port, String name) {
            this.address = address;
            this.port = port;
            this.name = name;
        }
    }
}