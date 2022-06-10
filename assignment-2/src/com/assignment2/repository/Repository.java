package com.assignment2.repository;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.assignment2.core.IDistributedRepository;

public class Repository implements IDistributedRepository {
    HashMap<String, List<Integer>> data;

    public Repository() {
        this.data = new HashMap<>();
    }

    @Override
    public void set(String key, Integer value) throws RemoteException {
        this.data.put(key, new ArrayList<>(Arrays.asList(value)));
    }

    @Override
    public void add(String key, Integer value) throws RemoteException {
        List<Integer> l = data.get(key);

        if (l != null)
            l.add(value);
        else {
            l = new ArrayList<>(Arrays.asList(value));
            this.data.put(key, l);
        }
    }

    @Override
    public List<Integer> get(String key) throws RemoteException {
        return data.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public void delete(String key) throws RemoteException {
        data.remove(key);
    }

    @Override
    public Integer getSize(String key) throws RemoteException {
        List<Integer> l = this.get(key);
        return l.size();
    }

    @Override
    public void reset() throws RemoteException {
        this.data = new HashMap<>();
    }

    @Override
    public Double avg(String key) throws RemoteException {
        return this.get(key).stream().mapToInt(val -> val).average().orElse(0.0);
    }

    @Override
    public Integer min(String key) throws RemoteException {
        try {
            return Collections.min(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public Integer max(String key) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer sum(String key) throws RemoteException {
        try {
            return Collections.max(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public Integer dsum(String[] repids) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer dmin(String[] repids) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer dmax(String[] repids) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double davg(String[] repids) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }
}