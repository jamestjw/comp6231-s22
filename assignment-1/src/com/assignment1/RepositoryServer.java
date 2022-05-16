package com.assignment1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RepositoryServer {
    ServerSocket sc;
    Repository repo;
    String repoId;
    SocketList socklist;
    MulticastSocket multicastSocket;
    HashMap<String, PeerDetails> peerDict = new HashMap<>();


    static String MULTICAST_ADDRESS = "230.0.0.0";
    static int MULTICAST_PORT = 6789;

    public RepositoryServer(int port, String id) throws IOException {
        this.sc = new ServerSocket(port);
        this.repoId = id;
        initialiseMulticast();
    }

    private void initialiseMulticast() throws IOException {
        // TODO: Make the address a constant too
        InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
        multicastSocket = new MulticastSocket(MULTICAST_PORT);
        multicastSocket.joinGroup(mcastaddr);
    }

    public int getPort() {
        return sc.getLocalPort();
    }

    public void start() {
        socklist = new SocketList();
        repo = new Repository();

        new Thread(new MainThreadHandler()).start();
        new Thread(new PeerDiscoveryServerHandler()).start();
        
        try {
            new Thread(new PeerSearcher()).start();
        } catch (IOException e) {
            reportError(e);
        }
    }

    public void stop() {

    }

    // Class that is responsible to search for peers
    private class PeerSearcher implements Runnable {
        public boolean stop = false;
        DatagramSocket socket;
        InetAddress group;

        public PeerSearcher() throws IOException {
            socket = new DatagramSocket();
            group = InetAddress.getByName(MULTICAST_ADDRESS);
        }

        public void run() {
            // Use one thread to discover peers
            new Thread(() -> discoverPeers()).start();
            // Use another one to handle responses
            new Thread(() -> savePeers()).start();
        }

        private void discoverPeers() {
            try {
                while(!stop) {
                    System.out.println(String.format("<%s> Discovering peers...", repoId));
                    // Send a discovery message once every 5 seconds
                    byte[] msg = getDiscoverMessage().getBytes();
                    DatagramPacket packet = new DatagramPacket(msg, msg.length, group, MULTICAST_PORT);
                    try {
                        socket.send(packet);
                    } catch (IOException e) {
                        reportError(e);
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // Ignore the fact that we have been interrupted
                    }
                }
            }
            finally {
                log(String.format("Server %s stopped. Print Repo id here!!"));
            }
        }

        private void savePeers() {
            byte[] buffer = new byte[1024];
            try {
                while(!stop) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length); 
                    try {
                        socket.receive(request);
                    } catch (IOException e) {
                        reportError(e);
                        continue;
                    }
                    String msg = new String(request.getData(), request.getOffset(), request.getLength());
                    String[] args = msg.split("\\s+");

                    if (args.length >= 2 && args[0].equals("ALIVE")) {
                        PeerDetails details = new PeerDetails(request.getAddress().getHostAddress(), request.getPort());
                        String peerID = args[1];

                        // TODO: Handle duplicates
                        peerDict.put(peerID, details);

                        System.out.println(String.format("<%s> Discovered peer %s", repoId, peerID));
                    }
                }
            }
            finally {
                log(String.format("Server %s stopped. Print Repo id here!!"));
            }
        }

        private String getDiscoverMessage() {
            return String.format("DISCOVER %s", repoId);
        }
    }

    // Class that is responsible for announcing the presence of this server
    // as a peer to others
    private class PeerDiscoveryServerHandler implements Runnable {
        public boolean stop = false;
        public void run() {
            byte[] buffer = new byte[1024];
            try {
                while(!stop) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    try {
                        multicastSocket.receive(packet);
                    }
                    catch(Exception ex) {
                        reportError(ex);
                    }

                    String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    String[] args = msg.split("\\s+");

                    if (args[0].equals("DISCOVER")) {
                        // We do not want to discover ourselves
                        if (args.length >= 2 && args[1].equals(repoId)) continue;

                        try {
                            reply(packet.getAddress(), packet.getPort());
                        } catch(Exception ex) {
                            reportError(ex);
                        }
                    }
                }
            }
            finally {
                log(String.format("Server %s stopped. Print Repo id here!!"));
            }
        }

        private void reply(InetAddress ip, int port) throws IOException {
            byte[] msg = buildPeerDiscoveryResponse().getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length,
               ip, port);
            multicastSocket.send(packet);
        }

        private String buildPeerDiscoveryResponse() {
            return String.format("ALIVE %s", repoId);
        }
    }

    private class MainThreadHandler implements Runnable {
        public boolean stop = false;
        public void run() {
            try {
                while(!stop) {
                    try {
                        Socket s = sc.accept(); // for non blocking see: java.nio.SocketChannel
                        if(stop) {
                            s.close();
                            return;
                        }
                        try {
                            socklist.add(s);
                            new Thread(new SafeRunnable(new SocketProtocol(repo, s), s)).start();
                            // TODO add thread to the threads list
                        }
                        catch(Exception ex) {
                            reportError(ex);
                        }
                    }
                    catch(Exception ex) {
                        reportError(ex);
                    }
                }
            }
            finally {
                log(String.format("Server %s stopped. Print Repo id here!!"));
            }
        }
    }

    private class SafeRunnable implements Runnable {
        private SocketProtocol o;
        private Socket s;
        private SafeRunnable(SocketProtocol o, Socket s) {
            this.o = o;
        }
        public void run() {
            try {
                o.run();
            }
            catch(Exception ex) {
                System.out.println("Error in SafeRunnable: " + ex.getMessage());
            }
            finally {
                try {
                    if (!s.isClosed())
                        s.close();
                }
                catch(Exception foo) {}
            }
        }
    }

    private class SocketList
    {
        private List<Socket> list = new ArrayList();

        private synchronized void add(Socket s) {
            list.add(s);
        }

        private void close() {
            while(true) {
                Socket s;
                synchronized (list) {
                    if (list.stream().count() <= 0)
                        return;
                    s = list.remove(0);
                }
                try {
                    s.close();
                }
                catch (Exception ex) {
                    reportError(ex);
                }
            }
        }
    }

    private void reportError(Exception ex) {
        String s = ex.getMessage();
        System.out.println(s);
        ex.printStackTrace();
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private class SocketProtocol {
        private Socket s;
        private Scanner scanner;
        private PrintWriter writer;
        private Repository repo;

        public SocketProtocol(Repository repo, Socket s) {
            this.s = s;
            this.repo = repo;

            try {
                this.scanner = new Scanner(s.getInputStream());
                this.writer = new PrintWriter(s.getOutputStream());
            }
            catch (Exception ex) {
                throw new RuntimeException("Socket I/O Error", ex);
            }
        }

        public void run() throws IOException {
            sendln(String.format("OK Repository <<%s>> ready", repoId));
            while (isConnected()) {
                String data = recvln();
                String[] args = data.split("\\s+");
                switch (args[0].toUpperCase()) {
                    case "SET":
                        if (args.length < 3) {
                            sendln("Invalid arguments, expected 'SET <identifier> <val> instead'");
                        } else {
                            try{
                                int number = Integer.parseInt(args[2]);
                                repo.set(args[1], number);
                                sendln("OK");
                            } catch (NumberFormatException ex){
                                sendln("Invalid value in SET command, expected INT value'");
                            }
                        }

                        break;
                    case "ADD":
                        if (args.length < 3) {
                            sendln("Invalid arguments, expected 'ADD <identifier> <val> instead'");
                        } else {
                            try{
                                int number = Integer.parseInt(args[2]);
                                repo.add(args[1], number);
                                sendln("OK");
                            } catch (NumberFormatException ex){
                                sendln("Invalid value in ADD command, expected INT value'");
                            }
                        }

                        break;
                    case "GET":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'GET <identifier> instead'");
                        } else {
                            List<Integer> values = repo.get(args[1]);
                            String listString = values.stream().map(String::valueOf).collect(Collectors.joining(", "));
                            sendln(String.format("OK %s", listString));
                        }

                        break;
                    case "SUM":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'DELETE <identifier> instead'");
                        } else {
                            int res = repo.sum(args[1]);
                            sendln(String.format("OK %d", res));
                        }

                        break;
                    case "DELETE":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'DELETE <identifier> instead'");
                        } else {
                            repo.delete(args[1]);
                            sendln("OK");
                        }

                        break;
                    case "QUIT":
                        sendln("CIAO Arrivederci!");
                        close();
                        return;
                    default:
                        sendln("ERR Sorry did not understand. Say QUIT if you wish to exit.");
                        break;
                }
            }
        }

        protected void sendln(String data) {
            writer.println(data); writer.flush();
        }
        protected String recvln() {
            return scanner.nextLine();
        }

        protected void close() throws IOException { s.close(); }

        protected boolean isConnected() { return s.isConnected(); }
    }

    private class PeerDetails {
        String address;
        int port;

        public PeerDetails(String address, int port) {
            this.address = address;
            this.port = port;
        }
    }
}