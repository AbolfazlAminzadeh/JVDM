package org.Kroj.Core.Tools.FileManagement;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class FileWriter extends FileObject {

    public FileWriter(Path path) {
        super(path.toString(),"rw");
    }

    public FileWriter(String path) {
        super(path,"rw");
    }

    public FileWriter(File f) {
        super(f,"w");
    }

    public FileWriter() throws IOException {
        super();
    }

    public void allocate(long byteLength) {
        try {
            getRandomAccessFile().setLength(byteLength);
        } catch (IOException e) {
            failed = true;
            logger.error().append("IO Exception while writing to file").nextLine();
        }
    }

    public FileWriter add(String s) {
        sb.append(s);return this;
    }
    public FileWriter add(char c) {
        sb.append(c);return this;
    }
    public FileWriter add(double d) {
        sb.append(d);return this;
    }
    public FileWriter add(float f) {
        sb.append(f);return this;
    }
    public FileWriter add(long l) {
        sb.append(l);return this;
    }
    public FileWriter add(boolean b) {
        sb.append(b);return this;
    }
    public FileWriter add(byte[] b, boolean isData) {
        if (isData) {
            sb.append(Arrays.toString(b));
            return this;
        }
        sb.append(new String(b));
        return this;
    }

    public FileWriter add(ByteBuffer b, boolean isData) {
        byte[] data = new byte[b.remaining()];b.get(data);
        return add(data,isData);
    }

    public FileWriter add(Object... o) {
        if (o.length == 0) {
            return this;
        }
        sb.append(Arrays.deepToString(o));
        return this;
    }
    public FileWriter add(Object o) {
        sb.append(o);return this;
    }
    public void send() {
        if (sb.isEmpty()) return;
        if (fc == null) {
            logger.error().append("The Channel is null, The program is not working properly, restart program to fix this").nextLine();
            return;
        }
        try {
            int w = fc.write(ByteBuffer.wrap(sb.toString().getBytes()));
            sb.setLength(0);
        } catch (IOException e) {
            logger.error().append("Error sending data to file, there is a problem please check it").nextLine();
        }
    }

}
