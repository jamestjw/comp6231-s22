package com.assignment2.core;

import java.util.List;

public interface IRepository extends IAggregate {
    public void set(String key, Integer value) throws RepException;

    public void add(String key, Integer value) throws RepException;

    public List<Integer> get(String key) throws RepException;

    public void delete(String key) throws RepException;

    public Integer getSize(String key) throws RepException;

    public void reset() throws RepException;
}