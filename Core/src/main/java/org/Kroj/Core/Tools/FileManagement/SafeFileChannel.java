package org.Kroj.Core.Tools.FileManagement;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SafeFileChannel implements AutoCloseable{

    private final Path path;
    private FileChannel channel;

    public SafeFileChannel(Path path) {
        this.path = path;
    }

    public synchronized void allocate(long size) throws IOException {
        Path folder = path.getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }

        this.channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
        );

        if (size > 0) {
            channel.truncate(size);
        }
    }

    public int write(ByteBuffer buffer, long position) throws IOException {
        return channel.write(buffer,position);
    }

    public void flush() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.force(true);
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
