package com.assignment2.core;

import java.rmi.Remote;

public interface IAggregate extends Remote {
    // Return average from list of values for specified list
    public Double avg(String key) throws RepException;

    public Integer min(String key) throws RepException;

    public Integer max(String key) throws RepException;

    public Integer sum(String key) throws RepException;
}