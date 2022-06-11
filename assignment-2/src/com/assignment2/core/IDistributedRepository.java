package com.assignment2.core;

import java.rmi.RemoteException;

public interface IDistributedRepository extends IRepository {
    public Integer dsum(String key, String[] repids) throws RemoteException;

    public Integer dmin(String[] repids) throws RemoteException;

    public Integer dmax(String[] repids) throws RemoteException;

    public Double davg(String[] repids) throws RemoteException;
}