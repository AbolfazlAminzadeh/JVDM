package org.Kroj.Core.Tools.FileManagement;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SafeFileChannel implements AutoCloseable {

    private final Path path;
    private volatile FileChannel channel;

    public SafeFileChannel(Path path) {
        this.path = path;
    }

    public void allocate(long size) throws IOException {
        Path folder = path.getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }

        this.channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.SPARSE
        );

        if (size > 0) {
            channel.truncate(size);
        }
    }

    private void ensureOpen() throws IOException {
        if (channel == null || !channel.isOpen()) {
            synchronized (this) {
                if (channel == null || !channel.isOpen()) {
                    this.channel = FileChannel.open(path,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.READ,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.SPARSE
                    );
                }
            }
        }
    }

    public void write(ByteBuffer buf, long pos) {
        try {
            ensureOpen();

            while (buf.hasRemaining()) {
                pos += channel.write(buf, pos);
            }

        } catch (IOException e) {
            throw new RuntimeException("I/O Error at pos: " + pos, e);
        }
    }

    public void flush() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.force(false);
        }
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    public FileChannel getChannel() {
        return channel;
    }
}