import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

import mpi.MPI;

public class Server {
    public static final int RMI_PORT = 6231;
    public static final String RMI_OBJ_NAME = "REPOSITORY";

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
            String rmiUrl = "rmi://localhost:6231/REPOSITORY";
            Master m = new Master(numSlaveNodes);

            RMIServer.start(RMI_PORT);
            RMIServer.register(m, RMI_OBJ_NAME);
        }
        // Slave process
        else {
            new Slave(rank).run();
        }

        MPI.Finalize();
    }
}
