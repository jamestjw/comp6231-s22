package com.assignment2.distribution;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import com.assignment2.core.Connector;
import com.assignment2.core.IRegistry;
import com.assignment2.core.IRepository;
import com.assignment2.core.RepException;

public class Registry extends UnicastRemoteObject implements IRegistry {
    String id; // ID that belongs to the Repo that owns this Registry
    // Key: Server ID
    // Value: RMI ID
    HashMap<String, String> servers = new HashMap<>();

    public Registry(String id) throws RemoteException {
        this.id = id;
    }

    @Override
    public synchronized IRepository find(String id) throws RemoteException {
        try {
            return Connector.getRepository(Connector.getRepositoryURI(id));
        } catch (Exception e) {
            throw new RepException(e);
        }
    }

    @Override
    public synchronized String[] list() throws RemoteException {
        return (String[]) servers.keySet().toArray();
    }

    @Override
    public synchronized void register(String id, String uri) throws RemoteException {
        this.servers.put(id, uri);
    }

    @Override
    public synchronized void unregister(String id) throws RemoteException {
        servers.remove(id);
    }
}