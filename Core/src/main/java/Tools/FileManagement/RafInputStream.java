package Tools.FileManagement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import static Tools.Logger.Logger.logger;

public class RafInputStream extends InputStream {

    protected RandomAccessFile raf;

    boolean error = false;

    public RafInputStream(String path)  {
        try {
            raf = new RandomAccessFile(path,"r");
        } catch (FileNotFoundException e) {
            logger.warn().append("Cannot Open InputStream From ").append(path).append(" Because: ").append(e.getMessage()).nextLine();
            error = true;
        }
    }

    @Override
    public int read() throws IOException {
        return raf.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    public boolean isError() {
        return error;
    }
}
