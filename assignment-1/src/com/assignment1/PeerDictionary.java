package com.assignment1;

import java.util.HashMap;

public class PeerDictionary {

    HashMap<String, PeerDetails> peerDict = new HashMap<>();

    public record PeerDetails(String address, int port) {
    }


    PeerDetails get(String key) {
        return peerDict.get(key);
    }

    void set(String key, String address, int port) {
        peerDict.put(key, new PeerDetails(address, port));
    }

}


