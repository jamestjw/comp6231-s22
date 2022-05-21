package com.assignment1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeerDictionary {
    List<String> blackList = new ArrayList<>();
    HashMap<String, PeerDetails> peerDict = new HashMap<>();

    public record PeerDetails(String address, int port) {
    }


    PeerDetails get(String key) {
        return peerDict.get(key);
    }

    void set(String key, String address, int port) {
        peerDict.put(key, new PeerDetails(address, port));
    }

    void deleteAndBlacklist(String key){
        peerDict.remove(key);
        blackList.add(key);
    }

}


