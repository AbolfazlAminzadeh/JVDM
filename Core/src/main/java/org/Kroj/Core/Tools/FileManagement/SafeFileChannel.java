package org.Kroj.Core.Tools.FileManagement;


import static org.Kroj.Core.Tools.Logger.Logger.logger;

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
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.SPARSE
        );

        if (size > 0) {
            ByteBuffer buf = ByteBuffer.allocate(1);
            buf.put((byte) 0);
            buf.flip();
            channel.write(buf, size - 1);
        }
    }

    public boolean isClosed() {
        return channel == null || !channel.isOpen();
    }

    public void write(ByteBuffer buf, long pos) {
        if (isClosed()) return;
        try {
            while (buf.hasRemaining()) {
                pos += channel.write(buf, pos);
            }
        } catch (IOException e) {
            logger.append("Error while writing data at pos").append(pos).append(e).nextLine();
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