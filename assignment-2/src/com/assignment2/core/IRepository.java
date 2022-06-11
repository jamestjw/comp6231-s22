package com.assignment2.core;

import java.rmi.RemoteException;
import java.util.List;

public interface IRepository extends IAggregate {
    public void set(String key, Integer value) throws RemoteException;

    public void add(String key, Integer value) throws RemoteException;

    public List<Integer> get(String key) throws RemoteException;

    public void delete(String key) throws RemoteException;

    public Integer getSize(String key) throws RemoteException;

    public void reset() throws RemoteException;
}