// Unable to run MPI with this :( I hate java
// package com.assignment3.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import mpi.MPI;

public class Master {
    int numSlaves;
    ArrayList<StorageLocation> slaveAvailableClusters;
    HashMap<String, FileEntry> records;

    public Master(int numSlaves) {
        this.numSlaves = numSlaves;
        this.slaveAvailableClusters = new ArrayList<>();
        this.records = new HashMap<>();

        for (int i = 1; i <= numSlaves; i++) {
            for (int j = 0; j < Slave.NUM_CLUSTERS; j++) {
                this.slaveAvailableClusters.add(new StorageLocation(i, j));
            }
        }

        writeLog(String.format("Active with %d slave nodes.", numSlaves));
    }

    /*
     * Handles a file upload
     */
    public void upload(String filename, int filesize, InputStream data) throws Master.InsufficientStorageException, Master.DuplicateFilenameException {
        ArrayList<StorageLocation> destinations;

        if (records.containsKey(filename))
            throw new DuplicateFilenameException("Filename already exists.");

        try {
            destinations = allocateStorageLocations(filesize);
            FileEntry entry = new FileEntry(filename, filesize);

            for (int i = 0; i < destinations.size(); i++) {
                StorageLocation destination = destinations.get(i);
                entry.addLocation(destination);
                uploadFilePart(filename, filesize, data, i, destination.slaveRank, destination.clusterNumber);
            }

            this.records.put(filename, entry);
        } catch (IOException e) {
            // TODO: Rethrow exception after handling cleanup
            e.printStackTrace();
        }
    }

    public void uploadFilePart(String filename, int filesize, InputStream data, int partNumber, int destinationRank,
            int destinationClusterNumber) throws IOException {
        byte buffer_send[] = generateByteArray(destinationClusterNumber, Slave.CLUSTER_SIZE, data);

        MPI.COMM_WORLD.Send(buffer_send, 0, Slave.WRITE_BUFFER_SIZE, MPI.BYTE, destinationRank, Slave.WRITE_TAG);

        MPI.COMM_WORLD.Recv(new byte[0], 0, 0, MPI.BYTE, destinationRank, Slave.SUCCESSFUL_WRITE_TAG);

        writeLog(String.format("Successfully written to node %d on cluster number %d.", destinationRank,
                destinationClusterNumber));
    }

    private static byte[] generateByteArray(int clusterNum, int dataLength, InputStream data) throws IOException {
        // Assume that integer has size of 4
        byte buffer[] = new byte[dataLength + 4];
        byte i[] = ByteBuffer.allocate(4).putInt(clusterNum).array(); // Put cluster num in a byte array

        System.arraycopy(i,
                0,
                buffer,
                0,
                4);

        int numBytes = data.read(buffer, 4, Slave.CLUSTER_SIZE);

        writeLog(String.format("Writing %d bytes", numBytes));

        return buffer;
    }

    private static void writeLog(String s) {
        System.out.println(String.format("<MASTER> %s", s));
    }

    private synchronized ArrayList<StorageLocation> allocateStorageLocations(int filesize)
            throws InsufficientStorageException {
        int numClustersRequired = (int) Math.ceil((double) filesize / Slave.CLUSTER_SIZE);

        if (numClustersRequired > slaveAvailableClusters.size()) {
            throw new InsufficientStorageException("Not enough storage");
        }

        ArrayList<StorageLocation> res = new ArrayList<>();

        Random rand = new Random();
        for (int i = 0; i < numClustersRequired; i++) {
            // Get random location from list and add it to the result
            int indexToGet = rand.nextInt(slaveAvailableClusters.size());
            StorageLocation location = slaveAvailableClusters.remove(indexToGet);
            res.add(location);

            writeLog(
                    String.format("Allocated node %d cluster %d.", location.slaveRank, location.clusterNumber));
        }

        return res;
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

        void addLocation(StorageLocation location) {
            directory.add(location);
        }
    }

    record StorageLocation(int slaveRank, int clusterNumber) {
    }

    public class InsufficientStorageException extends Exception {
        public InsufficientStorageException(String errorMessage) {
            super(errorMessage);
        }
    }

    public class DuplicateFilenameException extends Exception {
        public DuplicateFilenameException(String errorMessage) {
            super(errorMessage);
        }
    }
}
