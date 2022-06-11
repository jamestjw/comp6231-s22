package com.assignment2.core;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Connector {
    public static final int PORT_NUMBER = 1099;

    static public IDistributedRepository getRepository(String uri)
            throws RepException {
        try {
            return (IDistributedRepository) Naming.lookup(uri);
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            throw new RepException(e);
        }
    }

    static public IDistributedRepository getRepositoryByID(String id)
            throws RepException {
       return getRepository(getRepositoryURI(id));
    }


    static public IRegistry getRegistry(String uri)
            throws RepException {
        try {
            return (IRegistry) Naming.lookup(uri);
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            throw new RepException(e);
        }
    }

    // TODO: Do we need both directory and registry here?
    static public IDirectory getDirectory(String uri)
            throws RepException {
        try {
            return (IDirectory) Naming.lookup(uri);
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            throw new RepException(e);
        }
    }

    static public String getDirectoryURI(String objectID) {
        return getRegistryURI(objectID);
        // return String.format("rmi://localhost:%d/%s_DIRECTORY", PORT_NUMBER, objectID);
    }

    static public String getRegistryURI(String objectID) {
        return String.format("rmi://localhost:%d/%s_REGISTRY", PORT_NUMBER, objectID);
    }

    static public String getRepositoryURI(String objectID) {
        return String.format("rmi://localhost:%d/%s_REPOSITORY", PORT_NUMBER, objectID);
    }
}