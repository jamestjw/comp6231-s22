import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RepositoryProxy extends UnicastRemoteObject implements Repository {
    Master m;

    public RepositoryProxy(Master m) throws RemoteException {
        this.m = m;
    }

    @Override
    public List<String> listFiles() throws RemoteException {
        return m.listFiles();
    }

    @Override
    public void upload(String filename, int filesize, byte[] data) throws RemoteException, IOException,
            BrokenFileException, InsufficientStorageException, DuplicateFilenameException {
        m.upload(filename, filesize, data);
    }
}