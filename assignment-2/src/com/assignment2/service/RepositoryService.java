package com.assignment2.service;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.assignment2.core.Connector;
import com.assignment2.core.IRegistry;
import com.assignment2.core.RepException;
import com.assignment2.distribution.Registry;
import com.assignment2.repository.Repository;

public class RepositoryService {
    String id;
    Registry registry;

    public RepositoryService(String id) throws RepException {
        this.id = id;
        try {
            this.registry = new Registry(id);
        } catch (RemoteException e) {
            throw new RepException(e);
        }
    }

    public void start(String knownRepoID) throws RepException {
        try {
            // Construct remote registry for repository
            String registryURI = Connector.getRegistryURI(id);
            Naming.rebind(registryURI, registry);
            System.out.println(String.format("Remote registry for %s is ready", id));

            // Register registry object
            Repository exportedRepo = new Repository(id);
            String repoURI = Connector.getRepositoryURI(id);
            Naming.rebind(repoURI, exportedRepo);
            registry.register(id, repoURI);

            System.out.println(String.format("Remote repository for %s is ready", id));

            if (knownRepoID != null) {
                // Register this repo with another known repo
                IRegistry knownRepoRegistry = Connector.getRegistryByID(knownRepoID);
                knownRepoRegistry.register(id, repoURI);
            }
        } catch (RemoteException | MalformedURLException e) {
            throw new RepException(e);
        }
    }

    public void stop() throws RepException {
        try {
            // Unregister current repository from registries
            registry.unregister(id);

            // Unregister RMI registry
            Naming.unbind(Connector.getRegistryURI(id));

            // Unregister RMI repository
            Naming.unbind(Connector.getRepositoryURI(id));
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            throw new RepException(e);
        }
    }
}