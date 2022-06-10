package com.assignment2.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDirectory extends Remote {
    public IRepository find(String id) throws RemoteException;

    public String[] list() throws RemoteException;
}