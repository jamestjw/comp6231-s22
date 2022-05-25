import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Server {
    ServerSocket sc;
    String repoId;

    PeerDiscoveryProtocol pdp;

    public Server(int port, String id) throws IOException {
        this.sc = new ServerSocket(port);
        this.repoId = id;
    }

    public int getPort() {
        return sc.getLocalPort();
    }

    public void start() throws IOException {
        new Thread(new MainThreadHandler()).start();
         this.pdp = new PeerDiscoveryProtocol(repoId, getPort());
         pdp.start();

    }

    public void stop() {
    }

    private class MainThreadHandler implements Runnable {
        public boolean stop = false;
        public void run() {
            try {
                while(!stop) {
                    try {
                        Socket s = sc.accept(); // for non blocking see: java.nio.SocketChannel
                        if (stop) {
                            s.close();
                            return;
                        }
                        System.out.println(String.format("Accepted connection from %s", s.getInetAddress().getHostName()));
                        try {
                            new Thread(()-> {
                                try {
                                    new SocketProtocol(s).run();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }).start();
                        }
                        catch(Exception ex) {
                            Logger.getInstance().reportError(ex);
                        }
                    }
                    catch(Exception ex) {
                        Logger.getInstance().reportError(ex);
                    }
                }
            }
            finally {
                Logger.getInstance().log(String.format("Server %s stopped.",repoId));
            }
        }
    }

    private class SocketProtocol {
        private final Socket s;
        private final Scanner scanner;
        private final PrintWriter writer;

        public SocketProtocol(Socket s) {
            this.s = s;

            try {
                this.scanner = new Scanner(s.getInputStream());
                this.writer = new PrintWriter(s.getOutputStream());
            }
            catch (Exception ex) {
                throw new RuntimeException("Socket I/O Error", ex);
            }
        }

        public void run() throws IOException {
            sendln("SERVER RESPONSE " + repoId);
        }

        protected void sendln(String data) {
            writer.println(data); writer.flush();
        }
        protected String recvln() {
            try {
                return scanner.nextLine();
            } catch (NoSuchElementException e) {
                return "";
            }
        }

        protected void close() throws IOException { s.close(); }

        protected boolean isConnected() { return s.isConnected(); }
    }

    private static class RemoteCallHandler {
        private final Scanner scanner;
        private final PrintWriter writer;


        public RemoteCallHandler(String address, int port) {
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
            // Get the introductory text out of the way
            recvln();
            sendln(command);
            return recvln();
        }
    }
}