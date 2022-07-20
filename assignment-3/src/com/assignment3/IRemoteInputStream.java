import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface IRemoteInputStream extends Remote {
    public int read() throws IOException;

    public int available() throws IOException;

    public void close() throws IOException;

    public int read(byte[] b, int off, int len) throws IOException;
}