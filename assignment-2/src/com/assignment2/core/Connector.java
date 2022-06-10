package com.assignment2.core;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Connector {
    static public IRepository getRepository(String uri)
            throws RepException {
        try {
            return (IRepository) Naming.lookup(uri);
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            throw new RepException(e);
        }
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
}