import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream.Filter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import mpi.MPI;

public class Master implements Repository {
    private static boolean DEBUG_MODE = true;

    int numSlaves;
    ArrayList<StorageLocation> slaveAvailableClusters;
    HashMap<String, FileEntry> records;

    public Master(int numSlaves) throws RemoteException {
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

    public List<String> listFiles() {
        ArrayList<String> res = new ArrayList<>();

        for (FileEntry record : records.values()) {
            res.add(generateLogicalName(record.name));
        }

        return res;
    }

    /*
     * Deletes a file by removing the file entry and restoring the
     * availability of all of its storage locations
     */
    public synchronized void delete(String url) throws InvalidURLException, FileDoesNotExistException {
        String filename = parseURL(url);

        FileEntry entry = records.get(filename);

        if (entry == null)
            throw new FileDoesNotExistException("File does not exist");

        writeLog(String.format("Deleting file: %s", filename));

        for (StorageLocation location : entry.directory) {
            restoreStorageLocation(location);
        }

        records.remove(filename);


        if (DEBUG_MODE) printClusterInformation();
    }

    private synchronized void restoreStorageLocation(StorageLocation location) {
        slaveAvailableClusters.add(location);
        writeLog(String.format("Deleting cluster %d on node %d", location.clusterNumber,
                location.slaveRank));
    }

    /*
     * Handles a file upload
     */
    public void upload(String filename, int filesize, byte[] data)
            throws IOException, BrokenFileException, InsufficientStorageException, DuplicateFilenameException {
        ArrayList<StorageLocation> destinations = allocateStorageLocations(filesize);
        int destinationIndex = 0;

        if (records.containsKey(filename))
            throw new DuplicateFilenameException("Filename already exists.");

        try {
            FileEntry entry = new FileEntry(filename, filesize);

            for (; destinationIndex < destinations.size(); destinationIndex++) {
                StorageLocation destination = destinations.get(destinationIndex);
                entry.addLocation(destination);
                uploadFilePart(filename, filesize, data, destinationIndex, destination.slaveRank,
                        destination.clusterNumber);
            }

            this.records.put(filename, entry);
        } catch (IOException | BrokenFileException e) {
            writeLog("Error: Invalid file " + filename);
            writeLog("Error: " + e.getMessage());

            // Remove file parts that were successfully uploaded before
            for (int i = 0; i < destinationIndex; i++) {
                StorageLocation destination = destinations.get(i);
                restoreStorageLocation(destination);
            }

            throw e;
        }

        if (DEBUG_MODE) printClusterInformation();
    }

    /*
     * Downloads a file by giving the caller and OutputStream containing the file
     */
    public byte[] download(String url) throws InvalidURLException, FileDoesNotExistException, IOException {
        String filename = parseURL(url);

        FileEntry entry = records.get(filename);

        if (entry == null)
            throw new FileDoesNotExistException("File does not exist");

        writeLog(String.format("Handling request to download file: %s", filename));

        ByteArrayOutputStream res = new ByteArrayOutputStream(entry.size);

        for (int i = 0; i < entry.directory.size(); i++) {
            int size;

            // Last part may not be aligned to cluster size
            if (i == entry.directory.size() - 1) {
                size = entry.size % Slave.CLUSTER_SIZE;
            } else {
                size = Slave.CLUSTER_SIZE;
            }
            StorageLocation location = entry.directory.get(i);
            downloadFilePart(res, location.slaveRank, location.clusterNumber, size);
        }

        return res.toByteArray();
    }

    /*
     * This method accepts `clusterSize` because the last cluster could contain less
     * than
     * the max cluster size.
     */
    private void downloadFilePart(OutputStream os, int destinationRank, int destinationClusterNumber, int clusterSize)
            throws IOException {
        byte buffer_recv[] = new byte[Slave.CLUSTER_SIZE];
        int tag = (destinationClusterNumber << Slave.TAG_CLUSTER_NUM_SHIFT) | Slave.READ_TAG; 

        RMIServer.MPI_PROXY.Sendrecv(new byte[0], 0, 0, MPI.BYTE, destinationRank, tag, buffer_recv, 0, Slave.CLUSTER_SIZE, MPI.BYTE, destinationRank, tag);

        writeLog(String.format("Successfully downloaded cluster number %d from node %d.", destinationClusterNumber,
                destinationRank));

        os.write(buffer_recv, 0, clusterSize);
    }

    private void uploadFilePart(String filename, int filesize, byte[] data, int partNumber, int destinationRank,
            int destinationClusterNumber) throws IOException, BrokenFileException {
        int offset = partNumber * Slave.CLUSTER_SIZE;
        byte buffer_send[] = generateByteArray(Slave.CLUSTER_SIZE, data, offset);

        int tag = (destinationClusterNumber << Slave.TAG_CLUSTER_NUM_SHIFT) | Slave.WRITE_TAG; 
        RMIServer.MPI_PROXY.Sendrecv(buffer_send, 0, Slave.WRITE_BUFFER_SIZE, MPI.BYTE, destinationRank, tag, new byte[0], 0, 0, MPI.BYTE, destinationRank, tag);

        writeLog(String.format("Successfully written to node %d on cluster number %d.", destinationRank,
                destinationClusterNumber));
    }

    /*
     * Given an input stream of file data, creates a byte array that is ready to be
     * sent to
     * Slave nodes via MPI
     */
    private byte[] generateByteArray(int dataLength, byte[] data, int offset)
            throws IOException, BrokenFileException {
        byte buffer[] = new byte[dataLength];
        int numBytes = Math.min(data.length - offset, Slave.CLUSTER_SIZE);

        if (numBytes <= 0)
            throw new BrokenFileException("File size does not match actual file");

        System.arraycopy(data,
                offset,
                buffer,
                0,
                numBytes);

        writeLog(String.format("Writing %d bytes", numBytes));

        return buffer;
    }

    private static void writeLog(String s) {
        System.out.println(String.format("<MASTER> %s", s));
    }

    /*
     * Given a filesize, generate a random list of file storage locations
     */
    private synchronized ArrayList<StorageLocation> allocateStorageLocations(int filesize)
            throws InsufficientStorageException {
        int numClustersRequired = (int) Math.ceil((double) filesize / Slave.CLUSTER_SIZE);

        if (numClustersRequired > slaveAvailableClusters.size()) {
            throw new InsufficientStorageException(String.format("Not enough storage, required %d clusters but only have %d remaining", numClustersRequired, slaveAvailableClusters.size()));
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

    private static String generateLogicalName(String filename) {
        return String.format("//magical-file-system/%s", filename);
    }

    private String parseURL(String url) throws InvalidURLException {
        Pattern pattern = Pattern.compile("//magical-file-system/(.*)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new InvalidURLException("Invalid file URL was given.");
        }
    }

    private void printClusterInformation() {
        String[][] data = new String[Slave.NUM_CLUSTERS][numSlaves];
        for (int cluster = 0; cluster < Slave.NUM_CLUSTERS; cluster++) {
            for (int node = 0; node < numSlaves; node++) {
                data[cluster][node] = "<empty>";
            }
        }

        for (FileEntry record: records.values()) {
            for (int i = 0; i < record.directory.size(); i++) {
                StorageLocation location = record.directory.get(i);
                data[location.clusterNumber][location.slaveRank - 1] = String.format("%s #%d", truncateString(record.name, 15), i + 1);
            }
        }
        
        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(45);
    
        table.getColumns().add(new AsciiTable.Column("Cluster #"));
        for (int i = 0; i < numSlaves; i++)
           table.getColumns().add(new AsciiTable.Column("Node " + (i + 1)));
    

        for (int cluster = 0; cluster < Slave.NUM_CLUSTERS; cluster++) {
            AsciiTable.Row row = new AsciiTable.Row();
            table.getData().add(row);
            row.getValues().add(String.valueOf(cluster + 1));

            for (int node = 0; node < numSlaves; node++) {
                row.getValues().add(data[cluster][node]);
            }
        }
    
        table.calculateColumnWidth();
        table.render();
    }

    private static String truncateString(String s, int maxLength) {
        return s.substring(0, Math.min(s.length(), maxLength)) + "...";
    }
}