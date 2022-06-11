package com.assignment2.service;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import com.assignment2.core.Connector;
import com.assignment2.core.RepException;
import com.assignment2.repository.Repository;

public class RepositoryService {
    public void start(String id) throws RepException {
        try {
            Repository exportedObj = new Repository(id);
            String registryURI = Connector.getRepositoryURI(id);
            Naming.rebind(registryURI, exportedObj);
            System.out.println(String.format("Server %s is ready", id));
        } catch (RemoteException | MalformedURLException e) {
            throw new RepException(e);
        }
    }
}