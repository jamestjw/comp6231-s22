package com.assignment2.client;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import com.assignment2.core.Connector;
import com.assignment2.core.IDistributedRepository;

public class Client {
    public static void main(String[] argv) throws Exception {
        try {
            IDistributedRepository r1 = Connector.getRepositoryByID("R1");
            IDistributedRepository r2 = Connector.getRepositoryByID("R2");
            IDistributedRepository r3 = Connector.getRepositoryByID("R3");

            String[] emptyArray = {};

            r1.reset();
            assert_eq(r1.get("A"), Collections.emptyList());

            r1.set("A", 5);
            assert_eq(r1.get("A"), Collections.singletonList(5));

            r1.add("A", 2);
            assert_eq(r1.get("A"), List.of(5, 2));
            assert_eq(r1.sum("A"), 7);
            assert_eq(r1.dsum("A", emptyArray), 7);

            assert_eq(r1.max("A"), 5);
            assert_eq(r1.min("A"), 2);
            assert_eq(r1.avg("A"), 3.5);

            assert_eq(r1.max("B"), null);
            assert_eq(r1.min("B"), null);
            assert_eq(r1.avg("B"), 0.0);

            r1.add("B", 10);
            assert_eq(r1.ls(), List.of("A", "B")); 

            r1.delete("A");
            assert_eq(r1.get("A"), Collections.emptyList());

            System.out.println("Single repo tests passed.");

            r1.reset();
            r2.reset();
            r3.reset();
            r1.add("A", 10);
            r2.add("A", 20);
            r3.add("A", 30);
            assert_eq(r1.dsum("A", new String[] { "R2", "R3" }), 60);
            assert_eq(r2.dsum("A", new String[] { "R1", "R3" }), 60);
            assert_eq(r3.dsum("A", new String[] { "R1", "R2" }), 60);

            assert_eq(r1.dmin("A", new String[] { "R2", "R3" }), 10);
            assert_eq(r2.dmin("A", new String[] { "R1", "R3" }), 10);
            assert_eq(r3.dmin("A", new String[] { "R1", "R2" }), 10);
            
            assert_eq(r1.dmax("A", new String[] { "R2", "R3" }), 30);
            assert_eq(r2.dmax("A", new String[] { "R1", "R3" }), 30);
            assert_eq(r3.dmax("A", new String[] { "R1", "R2" }), 30);

            assert_eq(r1.davg("A", new String[] { "R2", "R3" }), 20.0);
            assert_eq(r2.davg("A", new String[] { "R1", "R3" }), 20.0);
            assert_eq(r3.davg("A", new String[] { "R1", "R2" }), 20.0);

            System.out.println("Multi repo tests passed.");
        } catch (RemoteException e) {
            System.err.println("Unable to connect to repo R1: " + e.getMessage());
        }
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