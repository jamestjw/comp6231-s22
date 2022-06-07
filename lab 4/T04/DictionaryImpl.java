import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * An implementation of the remote object: DictionaryImpl.java
 * @author Pouria Roostaei
 */


public class DictionaryImpl implements Dictionary {
    @Override
    public Map<String, Integer> word(String line) throws RemoteException {
        Map<String, Integer> stats = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(line);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            stats.put(token, stats.getOrDefault(token, 0) + 1);
        }
        return stats;
    }

    @Override
    public Map<String, Integer> word(String[] strings) throws RemoteException {
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
