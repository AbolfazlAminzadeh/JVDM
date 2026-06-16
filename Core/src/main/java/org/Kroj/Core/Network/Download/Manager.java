package org.Kroj.Core.Network.Download;

import io.netty.channel.IoEventLoopGroup;
import org.Kroj.Core.Network.Download.Handlers.DownloadListener;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.URL.URL;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Manager {

    private static final Manager instance = new Manager(Initializer.DOWNLOADER_THREADS);
    public final IoEventLoopGroup io = NettyUtil.getEventLoopGroup();
    private final int concurrency;


    public Manager(int concurrency) {
        this.concurrency = concurrency;
    }

    public static Manager getInstance() {
        return instance;
    }

    public Download makeDownload(URI uri, String targetPath, DownloadListener listener, String... devices) {
        Path path = Path.of(targetPath);
        List<String> deviceList = Arrays.asList(devices);

        return new Download(
                uri,
                path,
                concurrency,
                deviceList,
                io,
                listener
        );
    }

    public Download makeDownload(String link, String targetPath, DownloadListener listener, String... devices) {
        URI uri = URL.getSafeURI(link);
        return makeDownload(uri,targetPath,listener,devices);
    }

    public void shutdown() {
        io.shutdownGracefully();
    }


}
