package com.assignment1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PeerDiscoveryServer implements Runnable {
    private DatagramSocket socket;
    private final String repoId;
    private final int repoServerPort;

    PeerDiscoveryServer(int broadcastPort, String repoId, int repoServerPort) throws IOException {
        this.repoId = repoId;
        this.repoServerPort = repoServerPort;

        // https://michieldemey.be/blog/network-discovery-using-udp-broadcast/
        InetSocketAddress addr = new InetSocketAddress("0.0.0.0", broadcastPort);
        socket = new DatagramSocket(null);
        socket.setBroadcast(true);
        socket.setReuseAddress(true);
        socket.bind(addr);
    }

    public void run() {
        byte[] buffer = new byte[1024];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    socket.receive(packet);
                } catch (Exception ex) {
                    Logger.getInstance().reportError(ex);
                }

                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
                String[] args = msg.split("\\s+");

                if (args[0].equals("DISCOVER")) {
                    // We do not want to discover ourselves
                    if (args.length >= 2 && args[1].equals(repoId)) continue;

                    try {
                        reply(packet.getAddress(), packet.getPort());
                    } catch (Exception ex) {
                        Logger.getInstance().reportError(ex);
                    }
                }
            }
        } finally {
            Logger.getInstance().log(String.format("Server %s stopped.", repoId));
        }
    }

    private void reply(InetAddress ip, int port) throws IOException {
        byte[] msg = buildPeerDiscoveryResponse().getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                ip, port);
        socket.send(packet);
    }

    private String buildPeerDiscoveryResponse() {
        return String.format(
                "ALIVE %s %d",
                repoId,
                repoServerPort // TCP Port
        );
    }
}
