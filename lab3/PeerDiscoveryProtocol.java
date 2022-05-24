import java.io.IOException;

public class PeerDiscoveryProtocol {
    private final String repoId;
    private final int repoServerPort;
    private final int broadcastPort = 8888;

    public PeerDiscoveryProtocol(String repoId, String address, int repoServerPort) {
        this.repoId = repoId;
        this.repoServerPort = repoServerPort;
    }

    public void start() throws IOException {
        new Thread(new PeerDiscoveryServer(broadcastPort, repoId, repoServerPort)).start();
    }
}
