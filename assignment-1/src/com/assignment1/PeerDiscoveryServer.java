package com.assignment1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class PeerDiscoveryServer implements Runnable {

    private final String multicastAddress;
    private final int multicastPort;
    private MulticastSocket multicastSocket;
    private final String repoId;
    private final int repoServerPort;
    private final String repoServerAddress;

    private void initializeMulticast() throws IOException {
        // TODO: Make the address a constant too
        InetAddress mcastaddr = InetAddress.getByName(multicastAddress);
        multicastSocket = new MulticastSocket(multicastPort);
        multicastSocket.joinGroup(mcastaddr);
    }

    PeerDiscoveryServer(String multicastAddress, int multicastPort, String repoId, String repoServerAddress, int repoServerPort) throws IOException {
        this.repoId = repoId;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.repoServerPort = repoServerPort;
        this.repoServerAddress = repoServerAddress;

        initializeMulticast();
    }

    //TODO: make public fucn to stop thread
    private final boolean stop = false;

    public void run() {
        byte[] buffer = new byte[1024];
        try {
            while (!stop) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    multicastSocket.receive(packet);
                } catch (Exception ex) {
                    reportError(ex);
                }

                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
                String[] args = msg.split("\\s+");

                if (args[0].equals("DISCOVER")) {
                    // We do not want to discover ourselves
                    if (args.length >= 2 && args[1].equals(repoId)) continue;

                    try {
                        reply(packet.getAddress(), packet.getPort());
                    } catch (Exception ex) {
                        reportError(ex);
                    }
                }
            }
        } finally {
            log(String.format("Server %s stopped.", repoId));
        }
    }

    private void reply(InetAddress ip, int port) throws IOException {
        byte[] msg = buildPeerDiscoveryResponse().getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                ip, port);
        multicastSocket.send(packet);
    }

    private String buildPeerDiscoveryResponse() {
        return String.format(
                "ALIVE %s %s %d",
                repoId,
                repoServerAddress,
                repoServerPort // TCP Port
        );
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
