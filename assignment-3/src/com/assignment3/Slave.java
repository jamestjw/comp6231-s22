import java.nio.ByteBuffer;
import java.util.Arrays;

import mpi.MPI;
import mpi.Status;

public class Slave {
    public static final int CLUSTER_SIZE = 4096; // 4096 bytes
    public static final int NUM_CLUSTERS = 500;
    public static final int MAX_MEM = CLUSTER_SIZE * NUM_CLUSTERS; // Each node has 100 clusters

    public static final int READ_TAG = 0b001;
    public static final int WRITE_TAG = 0b010;
    public static final int TAG_MASK = 0b111;
    public static final int TAG_CLUSTER_NUM_SHIFT = 3; // Num bits in mask

    public static final int MASTER_RANK = 0;
    public static final int WRITE_BUFFER_SIZE = CLUSTER_SIZE;

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
        new Thread(() -> listenRequests()).start();
    }

    private void listenRequests() {
        writeLog("Listening for requests");

        while (true) {
           byte buffer_rcv[] = new byte[WRITE_BUFFER_SIZE];
           Status s = MPI.COMM_WORLD.Recv(buffer_rcv, 0, WRITE_BUFFER_SIZE, MPI.BYTE, MASTER_RANK, MPI.ANY_TAG);

           int clusterNumber = s.tag >> TAG_CLUSTER_NUM_SHIFT;
           int tag = s.tag & TAG_MASK;

           switch (tag) {
            case READ_TAG:
                handleRead(clusterNumber, s.tag);
                break;
            case WRITE_TAG:
                handleWrite(clusterNumber, s.tag, buffer_rcv);
                break;
            default:
                writeLog(String.format("Unexpected tag %d received, ignoring request", tag));
                break;
           }
       } 
    }

    private void handleRead(int clusterNumber, int originalTag) {
        byte buffer_send[] = new byte[CLUSTER_SIZE];

        writeLog(String.format("Reading cluster number %d.", clusterNumber));

        read(buffer_send, clusterNumber);

        // Send original tag back to master node
        MPI.COMM_WORLD.Send(buffer_send, 0, CLUSTER_SIZE, MPI.BYTE, MASTER_RANK, originalTag);
    }

    private void handleWrite(int clusterNumber, int originalTag, byte[] data) {
        writeLog(String.format("Writing to cluster number %d.", clusterNumber));

        write(data, clusterNumber);

        writeLog(String.format("Write to cluster number %d was successful.", clusterNumber));

        // Send original tag to master node
        MPI.COMM_WORLD.Send(new byte[0], 0, 0, MPI.BYTE, MASTER_RANK, originalTag);
    }

    /*
     * Writes the byte array into a certain cluster
     */
    private void write(byte[] d, int clusterNum) {
        assert clusterNum >= 0 && clusterNum < NUM_CLUSTERS : "Invalid clusterNum";
        System.arraycopy(d,
                0,
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
