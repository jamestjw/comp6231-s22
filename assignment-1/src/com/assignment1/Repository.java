package com.assignment1;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Repository {
    HashMap<String, List<Integer>> data;

    public Repository() {
        this.data = new HashMap<>();
    }

    public synchronized void set(String key, Integer value) {
        this.data.put(key, new ArrayList<>(Arrays.asList(value)));
    }

    public synchronized void add(String key, Integer value) {
        List<Integer> l = data.get(key);

        if (l != null)
            l.add(value);
        else {
            l = new ArrayList<>(Arrays.asList(value));
            this.data.put(key, l);
        }
    }

    public synchronized List<Integer> get(String key) {
        return data.getOrDefault(key, Collections.emptyList());
    }

    // Return average from list of values for specified list
    public synchronized Double avg(String key){
       return this.get(key).stream().mapToInt(val -> val).average().orElse(0.0);
    }

    public synchronized Integer min(String key){
        try {
            return Collections.min(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    public synchronized Integer max(String key){
        try {
            return Collections.max(this.get(key));
        } catch (java.util.NoSuchElementException e) {
            return null;
        }
    }

    public synchronized void delete(String key) {
        data.remove(key);
    }

    public synchronized Integer sum(String key) {
        List<Integer> l = this.get(key);
        return l.stream().reduce(0, Integer::sum);
    }
    public synchronized Integer getSize(String key) {
        List<Integer> l = this.get(key);
        return l.size();
    }

    public synchronized void reset() {
        this.data = new HashMap<>();
    }
}