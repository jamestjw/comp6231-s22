package com.assignment2.main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import com.assignment2.core.Connector;
import com.assignment2.core.RepException;
import com.assignment2.service.RepositoryService;

public class Main {
    public static void main(String[] argv) throws RemoteException {
        // TODO: Should this be here?
        LocateRegistry.createRegistry(Connector.PORT_NUMBER);

        String[] ids = { "R1", "R2", "R3", "R4", "R5" };
        String knownRepoID = null;
        for (String id : ids) {
            startRepo(id, knownRepoID);
            if (knownRepoID == null)
                knownRepoID = id;
        }
    }

    public static void startRepo(String currentRepoID, String knownRepoID) {
        System.out.println(String.format("Starting server %s using knownRepoID %s", currentRepoID, knownRepoID));

        RepositoryService repositoryService = new RepositoryService(currentRepoID);
        try {
            repositoryService.start(knownRepoID);
        } catch (RepException e) {
            System.err.println(String.format("Failed to start server %s: %s", currentRepoID, e.getMessage()));
        }
    }
}