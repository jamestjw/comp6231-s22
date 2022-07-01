// Unable to run MPI with this :( I hate java
// package com.assignment3.repository;

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
        int tag = 1;

        if (size < 2) {
            System.err.println("At least two nodes are required to run the repository.");
            System.exit(1);
        }

        // Master process
        if (rank == 0) {
            Master m = new Master();
            m.upload();
        }
        // Slave process
        else {
            new Slave(rank).run();
        }

        MPI.Finalize();
    }
}
