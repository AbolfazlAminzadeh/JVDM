package org.Kroj.Core.Network.Download;

import io.netty.channel.IoEventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.FileManagement.FileWriter;
import org.Kroj.Core.Tools.String.SizeManager;
import org.Kroj.Core.Tools.URL.URL;
import org.Kroj.UI.JavaFX.App;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class Manager {

    public IoEventLoopGroup mainGroup = NettyUtil.getEventLoopGroup();

    private static final Manager instance = new Manager(Initializer.DOWNLOADER_THREADS);

    private final List<String> devices = new CopyOnWriteArrayList<>();
    private final int concurrency;


    public Manager(int concurrency) {
        this.concurrency = concurrency;
    }

    public static Manager getInstance() {
        return instance;
    }

    public void startDownload(String link, String targetPath, DownloadListener listener, String... device) {
        startDownload(URL.getSafeURI(link),targetPath,listener,device);
    }

    public void startDownload(URI uri, String targetPath, DownloadListener listener, String... device) {
        devices.addAll(Arrays.asList(device));
        if (devices.isEmpty()) {
            logger.append("No devices available").nextLine();
            return;
        }

        Path path = Path.of(targetPath);
        boolean isDirectory = Files.isDirectory(path);

        Part headPart = new Part(uri, devices.getFirst(), 0, 0);
        Downloader head = new Downloader(headPart);

        head.start();

        head.contentLength.thenCombine(head.fileName, (size,rawFileName) -> {
                    if (size <= 0) {
                        logger.append("Invalid content length").nextLine();
                        return null;
                    }

                    String fileName = getFileName(uri, rawFileName);
                    Path finalPath = isDirectory ? path.resolve(fileName) : path;

                    FileWriter fw = new FileWriter(finalPath.toString());
                    fw.allocate(size);

                    boolean supportsRange = head.supportMultiThread;

//                    logger.append("Size: ").append(String.valueOf(size)).nextLine();
//                    logger.append("Range: ").append(String.valueOf(supportsRange)).nextLine();

                    if (listener != null) {
                        listener.onReady(fileName, size);
                    }

                    final List<Part> parts = getParts(headPart.getUri(), size, supportsRange);

                    final List<Downloader> downloading = new ArrayList<>(concurrency);

                    parts.forEach(part -> downloading.add(new Downloader(part,fw)));

                    ScheduledFuture<?> progressScheduler = mainGroup.scheduleAtFixedRate(() -> {
                        long current = 0;
                        double speed = 0;

                        for (Downloader downloader : downloading) {
                            current += downloader.getPart().getCurrentBytes();
                            speed += downloader.getSpeed();
                        }

                        if (listener != null) {
                            listener.onProgress(current, size, speed);
                        }
                    }, Initializer.PROGRESS_INTERVAL, Initializer.PROGRESS_INTERVAL, TimeUnit.MILLISECONDS);

                    final List<CompletableFuture<Void>> futures = new ArrayList<>();

                    for (Downloader downloader : downloading) {
                        futures.add(downloader.downloadFuture);
                        downloader.start();
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(
                            _ -> {
                                progressScheduler.cancel(false);
                                fw.finish();
                                if (listener != null) listener.onCompleted();
                            }).exceptionally(ex -> {
                        progressScheduler.cancel(false);
                        fw.finish();
                        if (listener != null) listener.onFailed(ex);
                        return null;
                    });

                    return null;
        }
        ).exceptionally(e -> {
            logger.append("Head Req failed: ").append(e.getMessage()).nextLine();
            if (listener != null) listener.onFailed(e);
            return null;
        });
    }

    public static String getFileName(final URI uri,final String cd) {
        logger.info();
        if (uri == null) return "";
        if (cd != null && !cd.isEmpty()) {
            int index = cd.toLowerCase().indexOf("filename=");
            if (index != -1) {
                String name = cd.substring(index+9).trim();
                if (name.startsWith("\"") && name.endsWith("\"") && name.length() > 2) {
                    name = name.substring(1, name.length() - 1);
                }
                return URLDecoder.decode(name, StandardCharsets.UTF_8);
            }
        }
        String p = uri.getPath();
        if (p == null || p.isEmpty() || p.equals("/")) return "downloaded_file."+UUID.randomUUID();
        int ls = p.lastIndexOf("/");
        return URLDecoder.decode(ls == -1 ? p : p.substring(ls + 1), StandardCharsets.UTF_8);
    }

    private List<Part> getParts(final URI url, final long size, final boolean ranged) {

        final List<Part> parts = new ArrayList<>();

        if (!ranged) {
            parts.add(new Part(url, devices.getFirst(), 0, -1));
            return parts;
        }

        final long partSize = size / concurrency;

        for (int i = 0; i < concurrency; i++) {
            long start = i * partSize;
            long end = (i == concurrency - 1)
                    ? size - 1
                    : (start + partSize - 1);

            parts.add(new Part(url, devices.get(i % devices.size()), start, end));
        }
        return parts;
    }

    static void main(String[] args) {
        if (args.length == 0) {
            App.launch(App.class,args);
            return;
        }
        String savePath = "~/Downloads/";
        String url = args[args.length - 1];
        if (URL.getSafeURI(url) == null) {
            logger.append("Usages:").nextLine();
            logger.append('\t').append("jvdm <url>");
            logger.append('\t').append("jvdm -o <path> <url>");
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o","--output":
                    if (args.length-1 >= i) {
                    savePath = args[i+1];
                    continue;
                    } else {
                        logger.append("Usage: -o <path>").nextLine();
                        logger.append("Example: -o ~/Downloads/").nextLine();
                        logger.append("Example: -o \"~/Downloads/Mamad.txt\"").nextLine();
                        System.exit(0);
                    }
                    break;
            }
        }
        BindToDeviceHandler.getDevices().forEach(logger::append);
        logger.nextLine();
        Manager.getInstance().startDownload(url, savePath, new DownloadListener() {
            @Override
            public void onReady(String fileName, long size) {
                logger.append(fileName).append(", ").append(size).nextLine();
            }

            @Override
            public void onProgress(long current, long total, double speed) {
                logger.append("\rDownload progress:\t").append(String.format("%.2f",((double)current/total)*100)).append('%').append(",\t").append(SizeManager.formatSpeed(speed)).append("\t\t\t").flush();
            }

            @Override
            public void onCompleted() {
                logger.nextLine().append("Download complete").nextLine();
//                System.exit(0);
            }

            @Override
            public void onFailed(Throwable onFailure) {
                logger.nextLine().append("Download Failed: ").append(onFailure.getLocalizedMessage()).nextLine();
//                System.exit(0);
            }
        }, BindToDeviceHandler.getDevices().toArray(String[]::new));

    }

}
