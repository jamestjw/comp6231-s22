package com.assignment2.distribution;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import com.assignment2.core.Connector;
import com.assignment2.core.IRegistry;
import com.assignment2.core.IRepository;
import com.assignment2.core.RepException;

public class Registry extends UnicastRemoteObject implements IRegistry {
    // Key: Server ID
    // Value: RMI ID
    HashMap<String, String> servers = new HashMap<>();

    public Registry() throws RemoteException {
    }

    @Override
    public IRepository find(String id) throws RemoteException {
        try {
            return Connector.getRepository(Connector.getRepositoryURI(id));
        } catch (Exception e) {
            throw new RepException(e);
        }
    }

    @Override
    public String[] list() throws RemoteException {
        return (String[]) servers.keySet().toArray();
    }

    @Override
    public void register(String id, String uri) throws RemoteException {
        this.servers.put(id, uri);
    }

    @Override
    public void unregister(String id) throws RemoteException {
        servers.remove(id);
    }
}