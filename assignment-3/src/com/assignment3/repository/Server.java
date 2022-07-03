// Unable to run MPI with this :( I hate java
// package com.assignment3.repository;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import mpi.MPI;
// import Slave;

public class Server {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: $MPJ_HOME/bin/mpjrun.sh -np 2 -cp output Server");
            return;
        }

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int numSlaveNodes = size - 1;

        if (size < 2) {
            System.err.println("At least two nodes are required to run the repository.");
            System.exit(1);
        }

        // Master process
        if (rank == 0) {
            // TODO: Here we should just host Master object with RMI interface
            Master m = new Master(numSlaveNodes);
            String filename = "test.txt";
            byte[] data = "testing".getBytes();
            int filesize = data.length;
            m.upload(filename, filesize, new ByteArrayInputStream(data));

            System.out.println("Listing files");
            for (String url : m.listFiles()) {
                System.out.println(url);
            }

            m.delete("//magical-file-system/test.txt");


            System.out.println("Listing files");
            for (String url : m.listFiles()) {
                System.out.println(url);
            }
        }
        // Slave process
        else {
            new Slave(rank).run();
        }

        MPI.Finalize();
    }
}
