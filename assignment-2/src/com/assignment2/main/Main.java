package com.assignment2.main;

import com.assignment2.core.RepException;
import com.assignment2.service.RepositoryService;

public class Main {
    public static void main(String[] argv) {
        RepositoryService repositoryService = new RepositoryService("R1");
        try {
            repositoryService.start();
        } catch (RepException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}