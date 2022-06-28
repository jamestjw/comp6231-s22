package com.assignment2.main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

import com.assignment2.core.Connector;
import com.assignment2.core.RepException;
import com.assignment2.service.RepositoryService;

public class Main {
    public static void main(String[] argv) throws RemoteException {
        // TODO: Should this be here?
        LocateRegistry.createRegistry(Connector.PORT_NUMBER);

        String[] ids = { "R1", "R2", "R3", "R4", "R5" };
        ArrayList<RepositoryService> services = new ArrayList<>();

        String knownRepoID = null;
        for (String id : ids) {
            services.add(startRepo(id, knownRepoID));
            if (knownRepoID == null)
                knownRepoID = id;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (RepositoryService service : services)
                try {
                    service.stop();
                } catch (RepException e) {
                    System.err.println(e.getMessage());
                } 
        }));
    }

    public static RepositoryService startRepo(String currentRepoID, String knownRepoID) throws RepException {
        System.out.println(String.format("Starting server %s using knownRepoID %s", currentRepoID, knownRepoID));

        RepositoryService repositoryService = new RepositoryService(currentRepoID);
        repositoryService.start(knownRepoID);

        return repositoryService;
    }
}