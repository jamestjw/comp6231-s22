package com.assignment1;

import com.assignment1.PeerDictionary.PeerDetails;

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

public class RepositoryServer {
    ServerSocket sc;
    Repository repo;
    String repoId;
    SocketList socklist;

    PeerDiscoveryProtocol pdp;

    public RepositoryServer(int port, String id) throws IOException {
        this.sc = new ServerSocket(port);
        this.repoId = id;
    }

    public String getAddress() {
        return sc.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return sc.getLocalPort();
    }

    public void start() throws IOException {
        socklist = new SocketList();
        repo = new Repository();

        new Thread(new MainThreadHandler()).start();
         this.pdp = new PeerDiscoveryProtocol(repoId, getAddress(), getPort());
         pdp.start();

    }

    public void stop() {
        socklist.close();
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
                            new Thread(()-> {
                                try {
                                    new SocketProtocol(repo, s).run();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }).start();
                            // TODO add thread to the threads list
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

    private class SocketList
    {
        private final List<Socket> list = new ArrayList<>();

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
                    Logger.getInstance().reportError(ex);
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
        private final Socket s;
        private final Scanner scanner;
        private final PrintWriter writer;
        private final Repository repo;

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

        // Since tuples do not exist, first elem is the actual key
        // Second elem is optionally the repo ID
        private String[] breakdownCommandKey(String arg) {
            Pattern r = Pattern.compile("(?:(\\w+)\\.)?(.*)");

            String[] res = new String[2];
            Matcher m = r.matcher(arg);

            // This regex search should always return results
            if (m.find()) {
                res[0] = m.group(2);
                res[1] = m.group(1);
            }

            return res;
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

                                String[] identifiers = breakdownCommandKey(args[1]);

                                if (identifiers[1] == null || identifiers[1].equals(repoId)) {
                                    repo.set(identifiers[0], number);
                                    sendln("OK");
                                } else {
                                    handleRemoteCommand(identifiers[1], data);
                                }
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

                                String[] identifiers = breakdownCommandKey(args[1]);

                                if (identifiers[1] == null || identifiers[1].equals(repoId)) {
                                    repo.add(identifiers[0], number);
                                    sendln("OK");
                                } else {
                                    handleRemoteCommand(identifiers[1], data);
                                }
                            } catch (NumberFormatException ex){
                                sendln("Invalid value in ADD command, expected INT value'");
                            }
                        }

                        break;
                    case "GET":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'GET <identifier> instead'");
                        } else {
                            String[] identifiers = breakdownCommandKey(args[1]);

                            List<Integer> values = repo.get(identifiers[0]);

                            if (identifiers[1] == null || identifiers[1].equals(repoId)) {
                                String listString = values.stream().map(String::valueOf).collect(Collectors.joining(", "));
                                sendln(String.format("OK %s", listString));
                            } else {
                                handleRemoteCommand(identifiers[1], data);
                            }
                        }

                        break;
                    case "SUM":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'DELETE <identifier> instead'");
                        } else {
                            String[] identifiers = breakdownCommandKey(args[1]);

                            if (identifiers[1] == null || identifiers[1].equals(repoId)) {
                                int res = repo.sum(identifiers[0]);
                                sendln(String.format("OK %d", res));
                            } else {
                                handleRemoteCommand(identifiers[1], data);
                            }
                        }

                        break;
                    case "DSUM":
                        Pattern pattern = Pattern.compile("DSUM\\s+(.*)\\s+INCLUDING\\s+(.*)");

                        Matcher m = pattern.matcher(data);

                        if (m.find()) {
                            String k = m.group(1);
                            int res = repo.sum(k);
                            String[] repos = m.group(2).split("\\s+");
                            boolean error = false;

                            for (String r: repos) {
                                // Ignore current repo
                                if (r.equals(repoId))
                                    continue;

                                PeerDictionary.PeerDetails details = pdp.getPeerDict().get(r);
                                if (details != null) {
                                    res += remoteGet(details, k);
                                } else {
                                    sendln(String.format("ERR Non-existence or ambiguous repository %s", r));
                                    error = true;
                                    break;
                                }
                            }

                            if (!error)
                                sendln(String.format("OK %d", res));
                        } else {
                            sendln("Expected format 'DSUM <identifier> INCLUDING <repo-1> <repo-2> ...'");
                        }

                        break;
                    case "DELETE":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'DELETE <identifier> instead'");
                        } else {
                            String[] identifiers = breakdownCommandKey(args[1]);

                            if (identifiers[1] == null || identifiers[1].equals(repoId)) {
                                repo.delete(identifiers[0]);
                                sendln("OK");
                            } else {
                                handleRemoteCommand(identifiers[1], data);
                            }
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

        protected void handleRemoteCommand(String remoteID, String command) {
            PeerDetails remoteDetails = pdp.getPeerDict().get(remoteID);
            // Remove reference to remote repository
            command = command.replace(remoteID+ ".", "");

            if (remoteDetails != null) {
                // Forward results to user
                sendln(
                    new RemoteCallHandler(
                        remoteDetails.address(),
                        remoteDetails.port()
                    ).runCommand(command)
                );
            } else {
                sendln(String.format("ERR Non-existence or ambiguous repository %s", remoteID));
            }
        }

        protected int remoteGet(PeerDetails remoteDetails, String key) {
            // Remove reference to remote repository
            String command = String.format("GET %s", key);
            String remoteRes = new RemoteCallHandler(
                            remoteDetails.address(),
                            remoteDetails.port()
                        ).runCommand(command);

            // We assume that there is a space after the OK
            Pattern r = Pattern.compile("OK\s+(.+)");
            Matcher m = r.matcher(remoteRes);

            int res = 0;
            if (m.find()) {
                String[] vals = m.group(1).split(",\\s+");

                for (String val: vals) {
                    res += Integer.parseInt(val);
                }
            }

            return res;
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