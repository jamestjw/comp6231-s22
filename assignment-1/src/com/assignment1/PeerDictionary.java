package com.assignment1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeerDictionary {
    List<String> blackList = new ArrayList<>();
    HashMap<String, PeerDetails> data = new HashMap<>();

    public record PeerDetails(String address, int port) {
    }

    PeerDetails get(String key) {
        return data.get(key);
    }

    void set(String key, String address, int port) {
        data.put(key, new PeerDetails(address, port));
    }

    void deleteAndBlacklist(String key){
        data.remove(key);
        blackList.add(key);
    }

    String[] getKeys() {
        return data.keySet().toArray(new String[0]);
    }
}


