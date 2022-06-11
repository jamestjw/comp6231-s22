package com.assignment2.service;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import com.assignment2.core.Connector;
import com.assignment2.core.RepException;
import com.assignment2.repository.Repository;

public class RepositoryService {
    String id;

    public RepositoryService(String id) {
        this.id = id;
    }

    public void start() throws RepException {
        try {
            LocateRegistry.createRegistry(Connector.PORT_NUMBER);

            Repository exportedObj = new Repository(id);
            String registryURI = Connector.getRepositoryURI(id);
            Naming.rebind(registryURI, exportedObj);
            System.out.println(String.format("Server %s is ready", id));
        } catch (RemoteException | MalformedURLException e) {
            throw new RepException(e);
        }
    }
}