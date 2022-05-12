package com.assignment1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RepositoryServer {
    ServerSocket sc;
    Repository repo;
    String repoId;
    SocketList socklist;

    public RepositoryServer(int port) throws IOException {
        this.sc = new ServerSocket(port);
    }

    public void start() {
        socklist = new SocketList();
        repo = new Repository();
        repoId = "R1";

        new Thread(new MainThreadHandler()).start();
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
                        if(stop) {
                            s.close();
                            return;
                        }
                        try {
                            socklist.add(s);
                            new Thread(new SafeRunnable(new SocketProtocol(repo, s), s)).start();
                            // TODO add thread to the threads list
                        }
                        catch(Exception ex) {
                            reportError(ex);
                        }
                    }
                    catch(Exception ex) {
                        reportError(ex);
                    }
                }
            }
            finally {
                log(String.format("Server %s stopped. Print Repo id here!!"));
            }
        }
    }

    private class SafeRunnable implements Runnable {
        private SocketProtocol o;
        private Socket s;
        private SafeRunnable(SocketProtocol o, Socket s) {
            this.o = o;
        }
        public void run() {
            try {
                o.run();
            }
            catch(Exception ex) {
                System.out.println("Error in SafeRunnable: " + ex.getMessage());
            }
            finally {
                try {
                    if (!s.isClosed())
                        s.close();
                }
                catch(Exception foo) {}
            }
        }
    }

    private class SocketList
    {
        private List<Socket> list = new ArrayList();

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
                    reportError(ex);
                }
            }
        }
    }

    private void reportError(Exception ex) {
        String s = ex.getMessage();
        System.out.println(s);
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private class SocketProtocol {
        private Socket s;
        private DataInputStream input;
        private DataOutputStream output;
        private Scanner scanner;
        private PrintWriter writer;
        private Repository repo;

        public SocketProtocol(Repository repo, Socket s) {
            this.s = s;
            this.repo = repo;

            try {
                this.input = new DataInputStream(s.getInputStream());
                this.output = new DataOutputStream(s.getOutputStream());
                this.scanner = new Scanner(s.getInputStream());
                this.writer = new PrintWriter(s.getOutputStream());
            }
            catch (Exception ex) {
                throw new RuntimeException("Socket I/O Error", ex);
            }
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
                                repo.set(args[1], number);
                                sendln("OK");
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
                                repo.add(args[1], number);
                                sendln("OK");
                            } catch (NumberFormatException ex){
                                sendln("Invalid value in ADD command, expected INT value'");
                            }
                        }

                        break;
                    case "GET":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'GET <identifier> instead'");
                        } else {
                            List<Integer> values = repo.get(args[1]);
                            String listString = values.stream().map(String::valueOf).collect(Collectors.joining(", "));
                            sendln(String.format("OK %s", listString));
                        }

                        break;
                    case "SUM":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'DELETE <identifier> instead'");
                        } else {
                            int res = repo.sum(args[1]);
                            sendln(String.format("OK %d", res));
                        }

                        break;
                    case "DELETE":
                        if (args.length < 2) {
                            sendln("Invalid arguments, expected 'DELETE <identifier> instead'");
                        } else {
                            repo.delete(args[1]);
                            sendln("OK");
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

        protected void sendln(String data) {
            writer.println(data); writer.flush();
        }
        protected String recvln() {
            return scanner.nextLine();
        }

        protected void close() throws IOException { s.close(); }

        protected boolean isConnected() { return s.isConnected(); }
    }
}