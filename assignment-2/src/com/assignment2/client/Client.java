package com.assignment2.client;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import com.assignment2.core.Connector;
import com.assignment2.core.IDistributedRepository;

public class Client {
    public static void main(String[] argv) throws Exception {
        try {
            IDistributedRepository repo = Connector.getRepositoryByID("R1");
            repo.reset();
            assert_eq(repo.get("A"), Collections.emptyList());

            repo.set("A", 5);
            assert_eq(repo.get("A"), Collections.singletonList(5));

            repo.add("A", 2);
            assert_eq(repo.get("A"), List.of(5, 2));
            assert_eq(repo.sum("A"), 7);

            assert_eq(repo.max("A"), 5);
            assert_eq(repo.min("A"), 2);
            assert_eq(repo.avg("A"), 3.5);

            assert_eq(repo.max("B"), null);
            assert_eq(repo.min("B"), null);
            assert_eq(repo.avg("B"), 0.0);

            repo.delete("A");
            assert_eq(repo.get("A"), Collections.emptyList());
        } catch (RemoteException e) {
            System.err.println("Unable to connect to repo R1: " + e.getMessage());
        }

        System.out.println("All tests passed.");
    }

    private static void assert_eq(Object a, Object b) throws Exception {
        if (a == null || b == null) {
            // Check if they are both null
            if (a != null || b != null) {
                throw new Exception(a + " != " + b);
            }
        } else {
            if (!a.equals(b)) {
                throw new Exception(a + " != " + b);
            }
        }
    }
}