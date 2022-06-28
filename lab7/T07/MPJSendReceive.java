
import mpi.*;
public class MPJSendReceive{
	public static void main(String[] args) throws Exception{
	       if (args.length < 5) {
            System.out.println("usage: $MPJ_HOME/bin/mpjrun.sh -np 2 -cp output MPJSendReceive <integer> <integer> ");
            return;
        }
        
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		int tag = 1;
		int dst;

		if(rank == 1){
			dst = 0;
			int buffer_rcv[] = new int [1];
			int buffer_snt[] = new int [1];
			buffer_snt[0] = Integer.parseInt(args[4]);
			MPI.COMM_WORLD.Send(buffer_snt, 0, 1, MPI.INT, dst, tag);
			System.out.println("My ID is <" + rank +"> sent # " + buffer_snt[0]);
			
			//TODO: implment Receive of the sum
			MPI.COMM_WORLD.Recv(buffer_rcv, 0, 1, MPI.INT, dst, tag);
			System.out.println("My ID is <" + rank +"> sum is " + buffer_rcv[0]);

		}

		else if(rank == 0){
			dst = 1;
			int buffer_snt[] = new int [1];
			int buffer_rcv[] = new int [1];

			MPI.COMM_WORLD.Recv(buffer_rcv, 0, 1, MPI.INT, dst, tag);
			System.out.println("My ID is <" + rank +"> received # " + buffer_rcv[0]);
			//TODO impleent addition of given integers
			int res = Integer.parseInt(args[3]);
			res += buffer_rcv[0];

			buffer_snt[0] = res;
			
			//TODO implement Send of the sum. 
			//
			System.out.println("My ID is <" + rank +"> sent sum " + buffer_snt[0]);
			MPI.COMM_WORLD.Send(buffer_snt, 0, 1, MPI.INT, dst, tag);
		}

		else{
			System.out.println("My ID is <" + rank +">. I'm noy doing anything");
		}
		MPI.Finalize();
	}
}

