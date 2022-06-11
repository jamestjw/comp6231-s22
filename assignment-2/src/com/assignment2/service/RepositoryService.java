package com.assignment2.service;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import com.assignment2.core.Connector;
import com.assignment2.core.RepException;
import com.assignment2.distribution.Registry;
import com.assignment2.repository.Repository;

public class RepositoryService {
    String id;

    public RepositoryService(String id) {
        this.id = id;
    }

    public void start() throws RepException {
        try {
            LocateRegistry.createRegistry(Connector.PORT_NUMBER);

            // Construct remote registry for repository
            Registry exportedRegistry = new Registry(id);
            String registryURI = Connector.getRegistryURI(id);
            Naming.rebind(registryURI, exportedRegistry);
            System.out.println(String.format("Remote registry for %s is ready", id));

            // Register registry object
            Repository exportedRepo = new Repository(id);
            String repoURI = Connector.getRepositoryURI(id);
            Naming.rebind(repoURI, exportedRepo);
            exportedRegistry.register(id, repoURI);

            System.out.println(String.format("Remote repository for %s is ready", id));

        } catch (RemoteException | MalformedURLException e) {
                        throw new RepException(e);
        }
    }
}