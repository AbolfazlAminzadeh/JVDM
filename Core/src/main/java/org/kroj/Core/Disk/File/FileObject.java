package org.kroj.Core.Disk.File;

import org.kroj.Core.Tools.ObjectManagement.ObjectsPool;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import static org.kroj.Core.Tools.Logger.Logger.logger;


public abstract class FileObject {
    protected final FileChannel fc;
    protected final RandomAccessFile f;
    protected final StringBuilder sb;

    protected boolean failed = false;

    public FileObject(String path,String mode) {
        FileChannel fc = null;
        RandomAccessFile raf = null;
        StringBuilder sb = null;
        try {
            sb = ObjectsPool.stringBuilders.get();
            raf = new RandomAccessFile(path, mode.toLowerCase());
            fc = raf.getChannel();
        } catch (IOException ee) {
            logger.error().append("I dont have access, or there is an error about IO, please check it, Go to log files for more info").nextLine().append(ee.getMessage()).nextLine();
            failed = true;
        }
        this.fc = fc;
        this.f = raf;
        this.sb = sb;
    }

    public FileObject(String directoryName, String fileName, String mode) {
        if (!directoryName.endsWith("/")) directoryName +="/";
        this(directoryName+fileName,mode.toLowerCase());
    }

    public FileObject(File file, String mode) {
        this(file.getAbsolutePath(),mode);
    }

    public FileObject() throws IOException {
        this(File.createTempFile("JVDM","tmp").getAbsolutePath(),"w");
    }

    public void finish() {
        try {
            ObjectsPool.stringBuilders.back(sb);
            fc.close();
        } catch (IOException e) {
            logger.error().append("I dont have access, or there is an error about IO, please check it: ").nextLine().append(e.getMessage()).nextLine();
        }
    }

    public RandomAccessFile getRandomAccessFile() {
        return f;
    }
    public FileChannel getFileChannel() {
        return fc;
    }

    public boolean isFailed() {
        return failed;
    }

    public static void main(String[] args) throws IOException {

    }
}
