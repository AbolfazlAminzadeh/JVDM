package org.Kroj.Core.Tools.FileManagement;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class SafeFileChannel implements AutoCloseable {

    private final Path path;
    private volatile FileChannel channel;
    private final ByteBuffer empty = ByteBuffer.wrap(new byte[]{0});
    public SafeFileChannel(Path path) {
        this.path = path;
    }

    public void allocate(long size) throws IOException {
        open(true);
        if (size > 0) {
            channel.write(empty,size-1);
        }
    }

    public void open() throws IOException{
        this.open(false);
    }

    private void open(boolean truncate) throws IOException{
        Path folder = path.getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }

        StandardOpenOption[] options = new StandardOpenOption[] {
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        };

        this.channel = FileChannel.open(path, truncate ? options : Arrays.copyOfRange(options,0,2));
    }

    public boolean isClosed() {
        return channel == null || !channel.isOpen();
    }

    public void write(ByteBuffer buf, long pos) throws IOException{
        if (isClosed()) throw new ClosedChannelException();
        while (buf.hasRemaining()) {
            pos += channel.write(buf, pos);
        }
    }


    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.force(false);
            channel.close();
        }
    }

    public FileChannel getChannel() {
        return channel;
    }
}