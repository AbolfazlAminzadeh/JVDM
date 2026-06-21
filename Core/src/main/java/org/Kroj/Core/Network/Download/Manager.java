package org.Kroj.Core.Network.Download;

import io.netty.channel.IoEventLoopGroup;
import org.Kroj.Core.Network.Download.Handlers.DownloadListener;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.Logger.Logger;
import org.Kroj.Core.Tools.NI.NetworkInterfaces;
import org.Kroj.Core.Tools.String.SizeManager;
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


    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) return;
        Manager.getInstance().makeDownload(args[args.length - 1], Initializer.DOWNLOAD_FOLDER, new DownloadListener() {
            @Override
            public void onReady(String fileName, long size) {
                Logger.logger.append("Head Received:").nextLine()
                        .append(fileName).nextLine()
                        .append(size).nextLine();
            }

            @Override
            public void onProgress(long current, long total, double speed) {
                Logger.logger.append('\r').append("Progress: ").append(
                        String.format("%.2f%%",(double) current / total * 100)).append('\t')
                        .append(", Speed: ").append(SizeManager.formatSpeed(speed))
                        .flush();
            }

            @Override
            public void onPaused(long lastByte, long total) {
                Logger.logger.append("Paused").nextLine();
            }

            @Override
            public void onCompleted() {
                Logger.logger.append("Complete!").nextLine();
//                System.exit(0);
            }

            @Override
            public void onFailed(Throwable onFailure) {
                Logger.logger.append("Failed").nextLine();
//                System.exit(0);
            }
        }, NetworkInterfaces.getDevices().toArray(String[]::new)).start();
    }

}
