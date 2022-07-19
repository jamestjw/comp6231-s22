import java.rmi.RemoteException;
import java.rmi.Remote;
import java.util.List;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.io.OutputStream;

public interface Repository extends Remote {
    public List<String> listFiles() throws RemoteException;

    public void delete(String url) throws RemoteException, InvalidURLException, FileDoesNotExistException;

    public void upload(String filename, int filesize, IRemoteInputStream input) throws RemoteException, IOException, BrokenFileException, InsufficientStorageException, DuplicateFilenameException, NoSuchAlgorithmException;

    public void download(String url, IRemoteOutputStream output) throws RemoteException, IOException, FileDoesNotExistException, InvalidURLException;
}