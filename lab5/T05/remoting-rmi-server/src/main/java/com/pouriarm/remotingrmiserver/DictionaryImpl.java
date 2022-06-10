package com.pouriarm.remotingrmiserver;
import com.pouriarm.remotingrmi.core.Dictionary;
import com.pouriarm.remotingrmi.core.DictonaryException;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class DictionaryImpl implements Dictionary{
    // Use the previous code base for this class. You need some modification for Error Handling!
    @Override
    public Map<String, Integer> word(String line) throws DictonaryException {
        Map<String, Integer> stats = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(line);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            stats.put(token, stats.getOrDefault(token, 0) + 1);
        }
        return stats;
    }

    @Override
    public Map<String, Integer> word(String[] strings) throws DictonaryException {
        Map<String, Integer> stats = new HashMap<>();
        for (String line: strings ) {
            Map<String, Integer> ret = word(line);
            for (Map.Entry<String, Integer> entry : ret.entrySet()) {
                String key = entry.getKey();
                stats.put(key, stats.getOrDefault(key, 0) + entry.getValue());
            }
        }
        return stats;
    }
}
