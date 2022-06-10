package com.assignment2.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAggregate extends Remote {
    // Return average from list of values for specified list
    public Double avg(String key) throws RemoteException;

    public Integer min(String key) throws RemoteException;

    public Integer max(String key) throws RemoteException;

    public Integer sum(String key) throws RemoteException;
}