package com.assignment2.core;

import java.rmi.RemoteException;

public interface IDistributedRepository extends IRepository {
    public Integer dsum(String key, String[] repids) throws RemoteException;

    public Integer dmin(String key, String[] repids) throws RemoteException;

    public Integer dmax(String key, String[] repids) throws RemoteException;

    public Double davg(String key, String[] repids) throws RemoteException;
}