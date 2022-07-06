package com.assignment2.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;

import com.assignment2.core.Connector;
import com.assignment2.core.IClientCallback;
import com.assignment2.core.IDistributedRepository;

public class Client extends UnicastRemoteObject implements IClientCallback {
    protected Client() throws RemoteException {
        super();
    }

    public void accessRemoteRepositories() throws Exception {
        try {
            IDistributedRepository r1 = Connector.getRepositoryByID("R1");
            IDistributedRepository r2 = Connector.getRepositoryByID("R2");
            IDistributedRepository r3 = Connector.getRepositoryByID("R3");

            String[] emptyArray = {};

            r1.reset();
            assert_eq(r1.get("A"), Collections.emptyList(), "Verify that A is empty");

            System.out.println("Setting A to 5");
            r1.set("A", 5);
            assert_eq(r1.get("A"), Collections.singletonList(5), "Verify that A contains 5 after setting it");

            System.out.println("Adding 2 to A");
            r1.add("A", 2);
            assert_eq(r1.get("A"), List.of(5, 2), "Verify A contains 5 and 2");
            assert_eq(r1.sum("A"), 7, "Sum of A is 7");
            assert_eq(r1.dsum("A", emptyArray), 7, "Distributed sum of A is 7");

            assert_eq(r1.max("A"), 5, "Max of A is 5");
            assert_eq(r1.min("A"), 2, "Min of A is 2");
            assert_eq(r1.avg("A"), 3.5, "Avg of A is 3.5");

            assert_eq(r1.max("B"), null, "Max of B is null");
            assert_eq(r1.min("B"), null, "Max of A is null");
            assert_eq(r1.avg("B"), 0.0, "Avg of A is 0.0");

            System.out.println("Adding 10, 9 and 8 to B");
            r1.add("B", 10);
            r1.add("B", 9);
            r1.add("B", 8);

            assert_eq(r1.ls(), List.of("A", "B"), "Verify list of keys includes just A and B");

            System.out.println("Caling enumerate keys");
            r1.enumerateKeys(this);
            System.out.println("Caling enumerate values on B");
            r1.enumerateValues("B", this);

            System.out.println("Deleting key A");
            r1.delete("A");
            assert_eq(r1.get("A"), Collections.emptyList(), "Verifying that after deleting A then the value is empty");

            System.out.println("Single repo tests passed.");

            r1.reset();
            r2.reset();
            r3.reset();
            System.out.println("Adding value 10, 20, 30 to repos r1, r2 and r3 respectively on the key A.");
            r1.add("A", 10);
            r2.add("A", 20);
            r3.add("A", 30);
            assert_eq(r1.dsum("A", new String[] { "R2", "R3" }), 60, "Verify DSUM on repo 1 is correct on key A");
            assert_eq(r2.dsum("A", new String[] { "R1", "R3" }), 60, "Verify DSUM on repo 2 is correct on key A");
            assert_eq(r3.dsum("A", new String[] { "R1", "R2" }), 60, "Verify DSUM on repo 3 is correct on key A");

            assert_eq(r1.dmin("A", new String[] { "R2", "R3" }), 10, "Verify dmin on repo 1 is correct on key A");
            assert_eq(r2.dmin("A", new String[] { "R1", "R3" }), 10, "Verify Dmin on repo 2 is correct on key A");
            assert_eq(r3.dmin("A", new String[] { "R1", "R2" }), 10, "Verify dmin on repo 3 is correct on key A");

            assert_eq(r1.dmax("A", new String[] { "R2", "R3" }), 30, "Verify dmax on repo 1 is correct on key A");
            assert_eq(r2.dmax("A", new String[] { "R1", "R3" }), 30, "Verify dmax on repo 2 is correct on key A");
            assert_eq(r3.dmax("A", new String[] { "R1", "R2" }), 30, "Verify dmax on repo 3 is correct on key A");

            assert_eq(r1.davg("A", new String[] { "R2", "R3" }), 20.0, "Verify davg on repo 1 is correct on key A");
            assert_eq(r2.davg("A", new String[] { "R1", "R3" }), 20.0, "Verify davg on repo 2 is correct on key A");
            assert_eq(r3.davg("A", new String[] { "R1", "R2" }), 20.0, "Verify davg on repo 3 is correct on key A");

            System.out.println("Multi repo tests passed.");
        } catch (RemoteException e) {
            System.err.println("Unable to connect to repo R1: " + e.getMessage());
        }
    }

    public static void main(String[] argv) throws Exception {
        new Client().accessRemoteRepositories();
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

    private static void assert_eq(Object a, Object b, String testName) throws Exception {
        System.out.print(String.format("%s... ", testName));
        assert_eq(a, b);
        System.out.println("PASSED");
    }

    @Override
    public void keyCallback(String key) throws RemoteException {
        System.out.println("Key callback: " + key);
    }

    @Override
    public void valueCallback(Integer value) throws RemoteException {
        System.out.println("Value callback: " + value);
    }
}