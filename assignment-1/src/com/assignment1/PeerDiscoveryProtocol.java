package com.assignment1;

import java.io.IOException;

public class PeerDiscoveryProtocol {
    static String MULTICAST_ADDRESS = "230.0.0.0";
    static int MULTICAST_PORT = 6789;

    private final String repoId;
    private final int repoServerPort;

    private final PeerDictionary peerDict = new PeerDictionary();

    public PeerDiscoveryProtocol(String repoId, int repoServerPort) {
        this.repoId = repoId;
        this.repoServerPort = repoServerPort;

    }

    public void start() throws IOException {

        new Thread(new PeerDiscoveryServer(MULTICAST_ADDRESS, MULTICAST_PORT, repoId, repoServerPort)).start();

        try {
            new Thread(new PeerSearcher(MULTICAST_ADDRESS, MULTICAST_PORT, repoId, peerDict)).start();
        } catch (IOException e) {
            reportError(e);
        }
    }
    private void reportError(Exception ex) {
        String s = ex.getMessage();
        System.out.println(s);
        ex.printStackTrace();
    }

    public PeerDictionary getPeerDict (){return this.peerDict;}
}
