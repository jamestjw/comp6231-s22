import java.nio.ByteBuffer;
import java.util.Arrays;

import mpi.MPI;

public class Slave {
    public static final int CLUSTER_SIZE = 4096; // 4096 bytes
    public static final int NUM_CLUSTERS = 500;
    public static final int MAX_MEM = CLUSTER_SIZE * NUM_CLUSTERS; // Each node has 100 clusters
    public static final int READ_TAG = 0b001;
    public static final int WRITE_TAG = 0b010;
    public static final int DELETE_TAG = 0b011;
    public static final int MASTER_RANK = 0;
    public static final int WRITE_BUFFER_SIZE = CLUSTER_SIZE + 4; // Use 1st 4 bytes to store cluster number

    private byte[] data = new byte[MAX_MEM];
    private int rank;

    public Slave(int rank) {
        this.rank = rank;
    }

    /*
     * The slave node listens for messages from the Master node and will respond to
     * requests
     * to read and write data to it.
     */
    public void run() {
        new Thread(() -> handleReads()).start();
        new Thread(() -> handleWrites()).start();
    }

    private void handleReads() {
        writeLog("Listening for reads");

        while (true) {
            int buffer_rcv[] = new int[1];
            byte buffer_send[] = new byte[CLUSTER_SIZE];
            MPI.COMM_WORLD.Recv(buffer_rcv, 0, 1, MPI.INT, MASTER_RANK, READ_TAG);
            int clusterNumber = buffer_rcv[0];

            writeLog(String.format("Reading cluster number %d.", clusterNumber));

            read(buffer_send, clusterNumber);

            // Send success tag to master node
            MPI.COMM_WORLD.Send(buffer_send, 0, CLUSTER_SIZE, MPI.BYTE, MASTER_RANK, READ_TAG);
        }
    }

    private void handleWrites() {
        writeLog("Listening for writes");

        while (true) {
             byte buffer_rcv[] = new byte[CLUSTER_SIZE + 4]; // Use first 4 bytes to indicate cluster number
            MPI.COMM_WORLD.Recv(buffer_rcv, 0, WRITE_BUFFER_SIZE, MPI.BYTE, MASTER_RANK, WRITE_TAG);

            int clusterNumber = ByteBuffer.wrap(buffer_rcv, 0, 4).getInt();

            writeLog(String.format("Writing to cluster number %d.", clusterNumber));

            write(buffer_rcv, clusterNumber);

            writeLog(String.format("Write to cluster number %d was successful.", clusterNumber));

            // Send success tag to master node
            MPI.COMM_WORLD.Send(new byte[0], 0, 0, MPI.BYTE, MASTER_RANK, WRITE_TAG);
        }
    }

    /*
     * Writes the byte array into a certain cluster
     */
    private void write(byte[] d, int clusterNum) {
        assert clusterNum >= 0 && clusterNum < NUM_CLUSTERS : "Invalid clusterNum";
        System.arraycopy(d,
                4, // Because the first 4 bytes denote the cluster number
                data,
                clusterStartIndex(clusterNum),
                CLUSTER_SIZE);
    }

    /*
     * Reads the byte array into a certain cluster
     */
    private void read(byte[] d, int clusterNum) {
        assert clusterNum >= 0 && clusterNum < NUM_CLUSTERS : "Invalid clusterNum";
        System.arraycopy(
                data,
                clusterStartIndex(clusterNum),
                d,
                0,
                CLUSTER_SIZE);
    }

    private static int clusterStartIndex(int clusterNum) {
        return CLUSTER_SIZE * clusterNum;
    }

    private void writeLog(String s) {
        System.out.println(String.format("<SLAVE %d> %s", rank, s));
    }
}
