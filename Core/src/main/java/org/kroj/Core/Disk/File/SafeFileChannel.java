package org.kroj.Core.Disk.File;


import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

    private void open(boolean truncate) throws IOException {
        Path folder = path.getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        if (truncate) this.channel = FileChannel.open(path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        else this.channel = FileChannel.open(path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE);

    }

    public boolean isClosed() {
        return channel == null || !channel.isOpen();
    }

    public void write(ByteBuf buf, long pos) throws IOException{
        if (isClosed()) throw new ClosedChannelException();
        ByteBuffer[] nioBuffers = buf.nioBuffers();

        for (ByteBuffer buffer : nioBuffers) {
            while (buffer.hasRemaining()) {
                pos += channel.write(buffer, pos);
            }
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