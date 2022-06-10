package com.assignment2.core;

public interface IRegistry extends IDirectory {
    public void register(String id, String uri) throws RepException;

    public void unregister(String id) throws RepException;
}