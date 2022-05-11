package com.assignment1;

import java.net.Socket;

import com.assignment1.socklib.NotificationHandler;
import com.assignment1.socklib.ProtocolFactory;
import com.assignment1.socklib.ServerListener;

public class RepositoryServerListener extends ServerListener {
    Repository repo;

    public RepositoryServerListener(String name, int port, ProtocolFactory pf, NotificationHandler handler, boolean dontThrowError) {
        super(name, port, pf, handler, dontThrowError);
        this.repo = new Repository();
    }

    public Repository getRepository() {
        return this.repo;
    }
}
