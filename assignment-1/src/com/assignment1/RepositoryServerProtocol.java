package com.assignment1;

import com.assignment1.socklib.ListenerInfo;
import com.assignment1.socklib.SimpleSocketProtocol;
import com.assignment1.RepositoryServerListener;

import java.io.IOException;
import java.net.Socket;
import java.lang.reflect.Method;

public class RepositoryServerProtocol extends SimpleSocketProtocol {
    Repository repo;

    public RepositoryServerProtocol(Socket s, ListenerInfo info, Repository r) {
        super(s, info);
        this.repo = r;
    }

    private Repository getRepository() {
        return this.repo;
    }

    public void run() throws IOException {
        if (getRepository() == null) {
            // This should never happen
            sendln("Unexpected error, server shutting down...");
            return;
        }
        sendln("OK Repository ready");
        // while (isRunning() && isConnected()) {
        //     String data = recvln();
        //     switch (data.toUpperCase()) {
        //         case "HELO":
        //             sendln(String.format("HELO %s; pleased to meet you!", getSocket().getRemoteSocketAddress().toString()));
        //             break;
        //         default:
        //             sendln("ERR Sorry did not understand. Say BYE if you wish to exit.");
        //             break;
        //     }
        // }
        close();
    }
}
