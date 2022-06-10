package com.assignment2.core;

public interface IDistributedRepository extends IRepository {
    public Integer dsum(String[] repids) throws RepException;

    public Integer dmin(String[] repids) throws RepException;

    public Integer dmax(String[] repids) throws RepException;

    public Double davg(String[] repids) throws RepException;
}