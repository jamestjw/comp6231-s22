package com.assignment2.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientCallback extends Remote {
    public void keyCallback(String key) throws RemoteException;

    public void valueCallback(Integer value) throws RemoteException;
}