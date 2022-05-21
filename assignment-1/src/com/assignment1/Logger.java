package com.assignment1;

import java.io.PrintStream;

public final class Logger {
    private static Logger instance;

    private PrintStream out;

    private Logger(PrintStream out) {
        this.out = out;
    }

    public static Logger getInstance(PrintStream out) {
        if (instance == null) {
            instance = new Logger(out);
        }
        return instance;
    }

    public static Logger getInstance() {
        return getInstance(System.out);
    }

    public void reportError(Exception ex) {
        out.println(ex.getMessage());
    }

    public void log(String msg) {
        out.println(msg);
    }
}