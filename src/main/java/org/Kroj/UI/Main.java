package org.Kroj.UI;

import org.Kroj.Core.Network.Download.DownloadListener;
import org.Kroj.Core.Network.Download.Manager;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Tools.String.SizeManager;
import org.Kroj.Core.Tools.URL.URL;
import org.Kroj.UI.JavaFX.App;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class Main {

    public static void main(String[] args) {
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