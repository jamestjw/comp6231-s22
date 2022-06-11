package com.assignment2.distribution;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map.Entry;

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
    public void register(String repoID, String uri) throws RemoteException {
        if (servers.containsKey(repoID)) {
            System.out.println(String.format("<Registry %s> Registering already known server %s", this.id, repoID));
            // We assume that if we have already added this server previously, then we can
            // assume
            // that we have already passed on the information to other servers.
        } else {
            System.out.println(String.format("<Registry %s> Registered %s URI: %s", this.id, repoID, uri));
            addServer(repoID, uri);

            for (Entry<String, String> entry : servers.entrySet()) {
                String key = entry.getKey();

                // Spread the information in this registration to all other servers
                if (!key.equals(this.id) && !key.equals(repoID)) {
                    Connector.getRegistryByID(key).register(repoID, uri);
                }

                // Pass info from this server to the registering server
                if (!key.equals(repoID)) {
                    Connector.getRegistryByID(repoID).register(key, entry.getValue());
                }
            }
        }
    }

    @Override
    public synchronized void unregister(String repoID) throws RemoteException {
        System.out.println(String.format("<Registry %s> Unregistered %s", this.id, repoID));
        servers.remove(repoID);
    }

    private synchronized void addServer(String repoID, String uri) {
        servers.put(repoID, uri);
    }
}