package com.assignment2.repository;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.assignment2.core.Connector;
import com.assignment2.core.IClientCallback;
import com.assignment2.core.IDirectory;
import com.assignment2.core.IDistributedRepository;
import com.assignment2.core.IRepository;

public class Repository extends UnicastRemoteObject implements IDistributedRepository {
    HashMap<String, List<Integer>> data;
    String id;

    public Repository(String id) throws RemoteException {
        this.data = new HashMap<>();
        this.id = id;
    }

    @Override
    public synchronized void set(String key, Integer value) {
        this.data.put(key, new ArrayList<>(Arrays.asList(value)));
    }

    @Override
    public synchronized void add(String key, Integer value) {
        List<Integer> l = data.get(key);

        if (l != null) {
            l.add(value);
        } else {
            l = new ArrayList<>(Arrays.asList(value));
            this.data.put(key, l);
        }
    }

    @Override
    public synchronized List<Integer> get(String key) {
        return data.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public synchronized void delete(String key) {
        data.remove(key);
    }

    @Override
    public synchronized void reset() {
        this.data = new HashMap<>();
    }

    @Override
    public synchronized Double avg(String key) {
        return this.get(key).stream().mapToInt(val -> val).average().orElse(0.0);
    }

    @Override
    public synchronized Integer min(String key) {
        try {
            return Collections.min(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public synchronized Integer max(String key) {
        try {
            return Collections.max(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public synchronized Integer sum(String key) {
        List<Integer> l = this.get(key);
        return l.stream().reduce(0, Integer::sum);
    }

    @Override
    public Integer dsum(String key, String[] repids) throws RemoteException {
        Integer sum = sum(key);
        IDirectory directory = Connector.getDirectory(Connector.getDirectoryURI(id));

        for (String repId : repids) {
            IRepository remoteRepo = directory.find(repId);
            sum += remoteRepo.sum(key);
        }
        return sum;
    }

    @Override
    public Integer dmin(String key, String[] repids) throws RemoteException {
        Integer currentMin = min(key);
        IDirectory directory = Connector.getDirectory(Connector.getDirectoryURI(id));

        for (String repId : repids) {
            IRepository remoteRepo = directory.find(repId);
            Integer remoteMin = remoteRepo.min(key);

            if (currentMin != null && remoteMin != null && remoteMin < currentMin) {
                currentMin = remoteMin;
            }
        }

        return currentMin;
    }

    @Override
    public Integer dmax(String key, String[] repids) throws RemoteException {
        Integer currentMax = max(key);
        IDirectory directory = Connector.getDirectory(Connector.getDirectoryURI(id));

        for (String repId : repids) {
            IRepository remoteRepo = directory.find(repId);
            Integer remoteMax = remoteRepo.max(key);

            if (currentMax != null && remoteMax != null && remoteMax > currentMax) {
                currentMax = remoteMax;
            }
        }

        return currentMax;
    }

    @Override
    public Double davg(String key, String[] repids) throws RemoteException {
        ArrayList<Integer> values = new ArrayList<Integer>(get(key));
        IDirectory directory = Connector.getDirectory(Connector.getDirectoryURI(id));

        for (String repId : repids) {
            IRepository remoteRepo = directory.find(repId);
            values.addAll(remoteRepo.get(key));
        }

        return values.stream()
                .mapToDouble(a -> a)
                .average()
                .orElse(0.0);
    }

    @Override
    public List<String> ls() throws RemoteException {
        return new ArrayList<>(data.keySet());
    }

    @Override
    public synchronized void enumerateKeys(IClientCallback reference) throws RemoteException {
      for (String key: data.keySet()) {
        reference.keyCallback(key);
      } 
    }

    @Override
    public synchronized void enumerateValues(String key, IClientCallback reference) throws RemoteException {
        for (Integer val: this.get(key)) {
            reference.valueCallback(val);
        }
    }
}