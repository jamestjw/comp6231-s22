import java.io.IOException;

public class PeerDiscoveryProtocol {
    private final String repoId;
    private final int repoServerPort;
    static final int BROADCAST_PORT = 8888;

    public PeerDiscoveryProtocol(String repoId, int repoServerPort) {
        this.repoId = repoId;
        this.repoServerPort = repoServerPort;
    }

    public void start() throws IOException {
        new Thread(new PeerDiscoveryServer(BROADCAST_PORT, repoId, repoServerPort)).start();
    }
}
