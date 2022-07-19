import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.io.OutputStream;
import java.rmi.server.UnicastRemoteObject;

public class RemoteOutputStream extends UnicastRemoteObject implements IRemoteOutputStream {
    private OutputStream output;
    public RemoteOutputStream(OutputStream output) throws RemoteException {
        this.output = output;
    }
    public void write(int b) throws IOException {
        this.output.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.output.write(b, off, len);
    }
}
