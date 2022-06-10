package com.assignment2.distribution;

import java.util.HashMap;

import com.assignment2.core.Connector;
import com.assignment2.core.IRegistry;
import com.assignment2.core.IRepository;
import com.assignment2.core.RepException;

public class Registry implements IRegistry {
    // Key: Server ID
    // Value: RMI ID
    HashMap<String, String> servers;

    @Override
    public IRepository find(String id) throws RepException {
        try {
            return Connector.getRepository(id);
        } catch (Exception e) {
            throw new RepException(e);
        }
    }

    @Override
    public String[] list() throws RepException {
        return (String[]) servers.keySet().toArray();
    }

    @Override
    public void register(String id, String uri) throws RepException {
        this.servers.put(id, uri);
    }

    @Override
    public void unregister(String id) throws RepException {
        servers.remove(id);
    }
}