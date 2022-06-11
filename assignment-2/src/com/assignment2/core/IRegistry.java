package com.assignment2.core;

import java.rmi.RemoteException;

public interface IRegistry extends IDirectory {
    public void register(String id, String uri) throws RemoteException;

    public void unregister(String id) throws RemoteException;
}