// Unable to run MPI with this :( I hate java
// package com.assignment3.repository;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import mpi.MPI;

public class Master {
    int numSlaves;
    int remainingAvailableClusters;
    HashMap<Integer, ArrayList<Integer>> slaveAvailableClusters;

    public Master(int numSlaves) {
        this.numSlaves = numSlaves;
        this.remainingAvailableClusters = numSlaves * Slave.NUM_CLUSTERS;
        this.slaveAvailableClusters = new HashMap<>();

        for (int i = 1; i <= numSlaves; i++) {
            ArrayList<Integer> l = new ArrayList<>(
                    IntStream.range(0, Slave.NUM_CLUSTERS).boxed().collect(Collectors.toList()));
            this.slaveAvailableClusters.put(i, l);
        }

        writeLog(String.format("Active with %d slave nodes.", numSlaves));
    }

    /*
     * Handles a file upload
     */
    public void upload(String filename, int filesize, InputStream data) {
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

    private synchronized List<StorageLocation> allocateStorageLocations(int filesize) {
        int numClustersRequired = (int) Math.ceil((double) filesize / Slave.CLUSTER_SIZE);

        if (numClustersRequired > remainingAvailableClusters) {
            throw new InsufficientStorageException("Not enough storage");
        }
    }

    private class FileEntry {
        private String name;
        private int size;
        private List<StorageLocation> directory;

        FileEntry(String name, int size) {
            this.name = name;
            this.size = size;
            this.directory = new ArrayList<>();
        }

        /*
         * This should be called in the right order, i.e. the first
         * time this is called the location data would correspond to the
         * first file part.
         */
        void addLocation(int slaveRank, int clusterNumber) {
            directory.add(new StorageLocation(slaveRank, clusterNumber));
        }
    }

    record StorageLocation(int slaveRank, int clusterNumber) {
    }

    public class InsufficientStorageException extends Exception {
        public InsufficientStorageException(String errorMessage) {
            super(errorMessage);
        }
    }
}
