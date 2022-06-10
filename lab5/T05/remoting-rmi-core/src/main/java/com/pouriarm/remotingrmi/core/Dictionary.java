package com.pouriarm.remotingrmi.core;

import java.util.Map;
/**
 * An interface for the remote object: Dictionary.java.
 * @author Pouria Roostaei
 */
public interface Dictionary {
    //TODO: Create two required methods (Hint: (String) and (String[]))
    public Map<String, Integer> word(String line) throws DictonaryException;
    public Map<String, Integer> word(String[] strings) throws DictonaryException;
}