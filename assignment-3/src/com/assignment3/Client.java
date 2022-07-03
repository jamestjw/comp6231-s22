import java.io.ByteArrayInputStream;
import java.rmi.Naming;

public class Client {
    public static void main(String[] argv) throws Exception {
        Repository r = (Repository) Naming.lookup("rmi://localhost:6231/REPOSITORY");


        String filename = "test222.txt";
        byte[] data = "testing".getBytes();
        int filesize = data.length;
        r.upload(filename, filesize, data);
        r.listFiles();
    }
}