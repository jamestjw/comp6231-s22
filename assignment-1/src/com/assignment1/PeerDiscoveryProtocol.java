package com.assignment1;

import java.io.IOException;

public class PeerDiscoveryProtocol {
    private final String repoId;
    private final int repoServerPort;
    private final int broadcastPort = 8888;

    private final PeerDictionary peerDict = new PeerDictionary();

    public PeerDiscoveryProtocol(String repoId, String address, int repoServerPort) {
        this.repoId = repoId;
        this.repoServerPort = repoServerPort;
    }

    public void start() throws IOException {
        new Thread(new PeerDiscoveryServer(broadcastPort, repoId, repoServerPort)).start();

        try {
            new Thread(new PeerSearcher(broadcastPort, repoId, peerDict)).start();
        } catch (IOException e) {
            reportError(e);
        }
    }
    private void reportError(Exception ex) {
        String s = ex.getMessage();
        System.out.println(s);
        ex.printStackTrace();
    }

    public PeerDictionary getPeerDict (){ return this.peerDict; }
}
