package com.assignment1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.assignment1.PeerDictionary.PeerDetails;

public class PeerSearcher implements Runnable {
    public boolean stop = false;
    DatagramSocket socket;
    String repoId;
    private final int broadcastPort;

    PeerDictionary peerDict;

    public PeerSearcher(int broadcastPort, String repoId, PeerDictionary peerDict) throws IOException {
        this.broadcastPort = broadcastPort;
        this.repoId = repoId;
        this.peerDict = peerDict;

        socket = new DatagramSocket();
        socket.setBroadcast(true);
    }

    public void run() {
        // Use one thread to discover peers
        new Thread(() -> {
            try {
                discoverPeers();
            } catch (UnknownHostException e) {
                stop();
                Logger.getInstance().reportError(e);
            }
        }).start();
        // Use another one to handle responses
        new Thread(this::savePeers).start();
    }

    private void discoverPeers() throws UnknownHostException {
        try {
            while (!stop) {
                // System.out.println(String.format("<%s> Discovering peers...", repoId));
                // Send a discovery message once every 5 seconds
                byte[] msg = getDiscoverMessage().getBytes();
                DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName("255.255.255.255"), broadcastPort);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    Logger.getInstance().reportError(e);
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Ignore the fact that we have been interrupted
                }
            }
        } finally {
            Logger.getInstance().log(String.format("Server %s stopped.", repoId));
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
                    Logger.getInstance().reportError(e);
                    continue;
                }

                String msg = new String(request.getData(), request.getOffset(), request.getLength());
                // Expected format: ALIVE RepoID Port
                Pattern r = Pattern.compile("ALIVE\s+(\\w+)\s+(\\d+)");
                Matcher m = r.matcher(msg);

                if (m.find()) {
                    String peerID = m.group(1);
                    String address = request.getAddress().getHostAddress();
                    int port = Integer.parseInt(m.group(2));
                    PeerDetails peerDetails = peerDict.get(peerID);
                    if (peerDetails != null) {
                        String currentAddress = peerDetails.address();
                        int currentPort = peerDetails.port();
                        if (!currentAddress.equals(address) || currentPort != port)
                            peerDict.deleteAndBlacklist(peerID);
                    } else peerDict.set(peerID, address, port);

                    // System.out.println(String.format("<%s> Discovered peer %s", repoId, peerID));
                }
            }
        } finally {
            Logger.getInstance().log(String.format("Server %s stopped.", repoId));
        }
    }

    private String getDiscoverMessage() {
        return String.format("DISCOVER %s", repoId);
    }

    private void stop() {
        stop = true;
    }
}

