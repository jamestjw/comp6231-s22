/*
 *  MPJAndRMIDemo.java
 *
 *  This example demonstrates using RMI with MPJ
 *  Rank 0 hosts the RMI object, by which any MPI messaging call
 *  is performed via RMIServer.MPI_PROXY, instead of MPI.COMM_WORLD.
 *  For simplicity, the master node performs a local RMI call via
 *  RMIClient.
 *
 *  see: MasterNode, SampleAPI, RMIClient
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 *  How to run:
 *      startup class: 	runtime.starter.MPJRun
 *      args: 		-jar $MPJ_HOME$\lib\starter.jar com.comp6231.MPJAndRMIDemo -np 3 -dev multicore
 *      env-var: 		MPJ_HOME=...
 *      add-to-class-path:	mpj-lib...
 */

import mpi.MPI;

import java.rmi.*;
import mpi.Status;

public class MPJAndRMIDemo {
    static final int PORT = 6231;
    static final int MASTER = 0;
    static final int DEST = 1;

    static final int NCLIENTS_TEST = 5;
    static final int BUF_LENGTH_TEST = 100;

    public static void main(String[] args) {
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        if (size < 2) {
            System.out.println("MPI size too low");
            return;
        }
        System.out.println("Hello world from <" + me + "> of <" + size + ">");
        if (me == MASTER) {
            MasterNode.main(args);
        }
        if (me == DEST) {
            ChildNode.main(args);
        }
        MPI.Finalize();
    }

    // API Remote Object

    interface APIInterface extends Remote {
        int foo(String a) throws RemoteException;

        void quit() throws RemoteException;
    }

    // API Implementation

    static class SampleAPI implements APIInterface {
        private static Integer callno = 0;

        public int foo(String a) throws RemoteException {
            int cn;
            synchronized (callno) {
                cn = ++callno;
            }
            System.out.println(String.format("** %d: calling foo", cn));
            System.out.println("Equation received from client " + cn + " is : " + a);
            char[] buf = new char[a.length()];
            for (int i = 0; i < a.length(); i++) {
                buf[i] = a.charAt(i);
            }
            int[] rbuf = new int[1];

            RMIServer.MPI_PROXY.Sendrecv(buf, 0, buf.length, MPI.CHAR, DEST, cn, rbuf, 0, 1, MPI.INT, DEST, cn);

            System.out.println(String.format("** %d: foo called", cn));

            return rbuf[0]; // receiving a message from DEST
        }

        public void quit() throws RemoteException {
            RMIServer.stop();
        }
    }

    static class MasterNode {
        private static void main(String[] args) {
            System.out.println("Master");
            System.out.println("TRY start listening");
            SampleAPI api = new SampleAPI();
            try {
                RMIServer.start(PORT);
                RMIServer.register(api);
                /****/
                System.out.println(String.format("TRY simulating RMI for %d 'processes'", NCLIENTS_TEST));
                for (int i = 0; i < NCLIENTS_TEST; i++) {
                    new Thread(() -> {
                        RMIClient.main(null);
                    }).start();
                }
                int[] buf = new int[BUF_LENGTH_TEST];
                MPI.COMM_WORLD.Recv(buf, 0, buf.length, MPI.INT, DEST, 0);
                api.quit(); // RMIServer.stop();
            } catch (Exception e) {
                System.out.println("ERR " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    static class ChildNode {
        public static void main(String[] args) {
            char[] buf = new char[10];
            int[] foo = new int[BUF_LENGTH_TEST];

            System.out.println("Child node is listening");

            for (int i = 0; i < NCLIENTS_TEST; i++) {
                Status s = MPI.COMM_WORLD.Recv(buf, 0, buf.length, MPI.CHAR, MASTER, MPI.ANY_TAG);
                String eq = new String(buf);
                System.out.println("Received problem: " + eq + " with tag " + s.tag);
                int res[] = new int[] { Problem.calculateEq(eq) };
                MPI.COMM_WORLD.Send(res, 0, 1, MPI.INT, MASTER, s.tag);
            }
            // sending signal to master to terminate the simulation
            MPI.COMM_WORLD.Isend(foo, 0, foo.length, MPI.INT, MASTER, 0);
        }

    }

    static class RMIClient {
        public static void main(String[] args) {
            try {
                System.out.println("OK looking up");
                APIInterface remoteapi = (APIInterface) Naming
                        .lookup(RMIServer.getURI(MPJAndRMIDemo.PORT, "SampleAPI"));
                String eq = Problem.generateEq();
                int res = remoteapi.foo(eq);
                System.out.println(String.format("OK called foo " + eq + " through RMI, result: %d", res));
            } catch (Exception e) {
                System.out.println("ERR " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}