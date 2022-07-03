import java.rmi.RemoteException;
import java.rmi.Remote;
import java.io.OutputStream;
import java.util.List;
import java.io.IOException;

public interface Repository extends Remote {
    public List<String> listFiles() throws RemoteException;

    // public void delete(String url) throws RemoteException, InvalidURLException, FileDoesNotExistException;

    public void upload(String filename, int filesize, byte[] data) throws RemoteException, IOException, BrokenFileException, InsufficientStorageException, DuplicateFilenameException;

    // public OutputStream download(String url) throws RemoteException, IOException, FileDoesNotExistException, InvalidURLException;
}