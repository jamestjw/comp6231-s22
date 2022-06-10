package com.assignment2.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.assignment2.core.Connector;
import com.assignment2.core.IDirectory;
import com.assignment2.core.IDistributedRepository;
import com.assignment2.core.IRepository;
import com.assignment2.core.RepException;

public class Repository implements IDistributedRepository {
    HashMap<String, List<Integer>> data;
    String id;

    public Repository(String id) {
        this.data = new HashMap<>();
        this.id = id;
    }

    @Override
    public void set(String key, Integer value) throws RepException {
        this.data.put(key, new ArrayList<>(Arrays.asList(value)));
    }

    @Override
    public void add(String key, Integer value) throws RepException {
        List<Integer> l = data.get(key);

        if (l != null)
            l.add(value);
        else {
            l = new ArrayList<>(Arrays.asList(value));
            this.data.put(key, l);
        }
    }

    @Override
    public List<Integer> get(String key) throws RepException {
        return data.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public void delete(String key) throws RepException {
        data.remove(key);
    }

    @Override
    public Integer getSize(String key) throws RepException {
        List<Integer> l = this.get(key);
        return l.size();
    }

    @Override
    public void reset() throws RepException {
        this.data = new HashMap<>();
    }

    @Override
    public Double avg(String key) throws RepException {
        return this.get(key).stream().mapToInt(val -> val).average().orElse(0.0);
    }

    @Override
    public Integer min(String key) throws RepException {
        try {
            return Collections.min(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public Integer max(String key) throws RepException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer sum(String key) throws RepException {
        try {
            return Collections.max(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public Integer dsum(String key, String[] repids) throws RepException {
        Integer sum = sum(key);
        IDirectory directory = Connector.getDirectory(Connector.getDirectoryURI(id));

        for (String repId: repids) {
            IRepository remoteRepo = directory.find(repId);
            sum += remoteRepo.sum(key);
        }
        return sum;
    }

    @Override
    public Integer dmin(String[] repids) throws RepException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer dmax(String[] repids) throws RepException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double davg(String[] repids) throws RepException {
        // TODO Auto-generated method stub
        return null;
    }
}