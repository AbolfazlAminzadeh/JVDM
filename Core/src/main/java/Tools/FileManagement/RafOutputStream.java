package org.Kroj.Core.Tools.FileManagement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class RafOutputStream extends OutputStream {

    protected RandomAccessFile raf;

    private boolean error = false;


    public RafOutputStream(String path) {
        try {
            raf = new RandomAccessFile(path,"rw");
        } catch (FileNotFoundException e) {
            logger.error().append("Cannot Open OutputStream From ").append(path).append(" Because: ").append(e.getMessage()).nextLine();
            error = true;
        }
    }

    @Override
    public void write(int b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }

    public void returnToStart() {
        try {
            raf.seek(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isError() {
        return error;
    }


}
