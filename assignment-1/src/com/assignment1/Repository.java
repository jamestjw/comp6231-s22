package com.assignment1;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Repository {
    HashMap<String, List<Integer>> data;

    public Repository() {
        this.data = new HashMap<String, List<Integer>>();
    }

    public void set(String key, Integer value) {
        List<Integer> l = data.get(key);

        if (l != null)
            l.add(value);
        else {
            l = new ArrayList<Integer>(Arrays.asList(value));
            this.data.put(key, l);
        }
    }

    public List<Integer> get(String key) {
        return data.getOrDefault(key, Collections.emptyList());
    }

    public void delete(String key) {
        data.remove(key);
    }

    public Integer sum(String key) {
        List<Integer> l = this.get(key);
        return l.stream().reduce(0, Integer::sum);
    }
}