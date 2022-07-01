// Unable to run MPI with this :( I hate java
// package com.assignment3.repository;

import java.nio.ByteBuffer;

import mpi.MPI;

public class Master {
    /*
     * Handles a file upload
     */
    public void upload() {
        int dest = 1;
        int destCluster = 4;
        byte buffer_send[] = generateByteArray(destCluster, Slave.CLUSTER_SIZE);

        MPI.COMM_WORLD.Send(buffer_send, 0, Slave.WRITE_BUFFER_SIZE, MPI.BYTE, dest, Slave.WRITE_TAG);

        MPI.COMM_WORLD.Recv(new byte[0], 0, 0, MPI.BYTE, dest, Slave.SUCCESSFUL_WRITE_TAG);

        writeLog(String.format("Successfully written to node %d on cluster number %d.", dest, destCluster));
    }

    private static byte[] generateByteArray(int clusterNum, int dataLength) {
        // Assume that integer has size of 4
        byte buffer[] = new byte[dataLength + 4];
        byte i[] = ByteBuffer.allocate(4).putInt(clusterNum).array(); // Put cluster num in a byte array

        System.arraycopy(i,
                0,
                buffer,
                0,
                4);

        return buffer;
    }

    private void writeLog(String s) {
        System.out.println(String.format("<MASTER> %s", s));
    }
}
