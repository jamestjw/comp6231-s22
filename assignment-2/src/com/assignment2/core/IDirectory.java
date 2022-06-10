package com.assignment2.core;

import java.rmi.Remote;

public interface IDirectory extends Remote {
    public IRepository find(String id) throws RepException;

    public String[] list() throws RepException;
}