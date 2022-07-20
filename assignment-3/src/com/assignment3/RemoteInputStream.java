import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.io.InputStream;
import java.rmi.server.UnicastRemoteObject;

public class RemoteInputStream extends UnicastRemoteObject implements IRemoteInputStream {
    private InputStream input;

    public RemoteInputStream(InputStream input) throws RemoteException {
        this.input = input;
    }

    public int read() throws IOException {
        return input.read();
    }

    public int available() throws IOException {
        return this.input.available();
    }

    public void close() throws IOException {
        this.close();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return input.read(b, off, len);
    }
}
