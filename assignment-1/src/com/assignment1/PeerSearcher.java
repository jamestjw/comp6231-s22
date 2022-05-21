package com.assignment1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PeerSearcher implements Runnable {
    public boolean stop = false;
    DatagramSocket socket;
    InetAddress group;
    String repoId;
    private final int multicastPort;


    //TODO: delete HashMap<String, PeerDetails> peerDict = new HashMap<>();
    PeerDictionary peerDict;

    public PeerSearcher(String multicastAddress, int multicastPort, String repoId, PeerDictionary peerDict) throws IOException {
        this.multicastPort = multicastPort;
        this.repoId = repoId;
        socket = new DatagramSocket();
        //TODO: RENAME group takes address
        group = InetAddress.getByName(multicastAddress);
        this.peerDict = peerDict;

    }

    public void run() {
        // Use one thread to discover peers
        new Thread(this::discoverPeers).start();
        // Use another one to handle responses
        new Thread(this::savePeers).start();
    }

    private void discoverPeers() {
        try {
            while (!stop) {
                // System.out.println(String.format("<%s> Discovering peers...", repoId));
                // Send a discovery message once every 5 seconds
                byte[] msg = getDiscoverMessage().getBytes();
                DatagramPacket packet = new DatagramPacket(msg, msg.length, group, multicastPort);
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
        } finally {
            log(String.format("Server %s stopped.", repoId));
        }
    }

    private void savePeers() {
        byte[] buffer = new byte[1024];
        try {
            while (!stop) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(request);
                } catch (IOException e) {
                    reportError(e);
                    continue;
                }
                String msg = new String(request.getData(), request.getOffset(), request.getLength());
                // Expected format: ALIVE RepoID Address Port
                Pattern r = Pattern.compile("ALIVE\s+(\\w+)\s+(.*)\s+(\\d+)");
                Matcher m = r.matcher(msg);

                if (m.find()) {

                    // TODO: Handle duplicates
                    String peerID = m.group(1);
                    peerDict.set(peerID, m.group(2), Integer.parseInt(m.group(3)));

                    // System.out.println(String.format("<%s> Discovered peer %s", repoId, peerID));
                }
            }
        } finally {
            log(String.format("Server %s stopped.", repoId));
        }
    }

    private String getDiscoverMessage() {
        return String.format("DISCOVER %s", repoId);
    }

    private void reportError(Exception ex) {
        String s = ex.getMessage();
        System.out.println(s);
        ex.printStackTrace();
    }

    //TODO: Introduce logger later
    private void log(String msg) {
        System.out.println(msg);
    }
}

