package org.Kroj.Core.Tools.FileManagement;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.Kroj.Core.Statics.Initializer.FILE_WRITER_CHUNK_SIZE;

public class MultiByteMapChannel implements AutoCloseable {

    private final Path path;
    private FileChannel channel;
    private MappedByteBuffer[] buffers;

    public MultiByteMapChannel(Path path) {
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
                StandardOpenOption.WRITE
        );

        channel.truncate(size);

        int bufCount = (int) ((size + FILE_WRITER_CHUNK_SIZE - 1) / FILE_WRITER_CHUNK_SIZE);
        this.buffers = new MappedByteBuffer[bufCount];

        long remaining = size;
        long pos = 0;
        for (int i = 0; i < bufCount;i++) {
            long chunkSize = Math.min(FILE_WRITER_CHUNK_SIZE,remaining);
            this.buffers[i] = channel.map(FileChannel.MapMode.READ_WRITE, pos, chunkSize);
            pos += chunkSize;
            remaining -= chunkSize;
        }
    }


    public void write(ByteBuffer buf, long pos) {
        int bufIndex = (int) (pos / FILE_WRITER_CHUNK_SIZE);
        int position = (int) (pos % FILE_WRITER_CHUNK_SIZE);

        MappedByteBuffer buffer = buffers[bufIndex];
        buffer.position(position);
        buffer.put(buf);
    }

    public void flush() {
        if (buffers != null) {
            for (MappedByteBuffer buffer : buffers) {
                if (buffer != null) {
                    buffer.force();
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        flush();
        if (channel != null && channel.isOpen()) channel.close();
    }

    public FileChannel getChannel() {
        return channel;
    }
}
