import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface IRemoteOutputStream extends Remote {
    default void write(byte b) throws RemoteException, IOException {
        write(new byte[] {b}, 0, 1);
    }

    void write(byte[] b, int off, int len) throws IOException;
}