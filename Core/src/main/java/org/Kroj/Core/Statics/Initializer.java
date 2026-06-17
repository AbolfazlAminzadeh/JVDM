package org.Kroj.Core.Statics;


import io.netty.util.concurrent.DefaultThreadFactory;
import org.Kroj.Core.Network.DNS.Resolver;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

public final class Initializer {

    // -=-=-=-=-=-=-=-=-=-=-=-
    //          JVDM
    // -=-=-=-=-=-=-=-=-=-=-=-

    public final static float VERSION = 1.3F;

    public final static int CPU_THREADS = Runtime.getRuntime().availableProcessors();

    public final static int JVDM_THREADS = CPU_THREADS * 2;

    public final static int FILE_WRITER_THREADS = CPU_THREADS / 2;

    public final static int SINGLE_THREAD_MAX_LENGTH = 1 << 19;

    public final static String DOWNLOAD_FOLDER = System.getProperty("user.home")+ File.separator + "Downloads";

    // -=-=-=-=-=-=-=-=-=-=-=-
    //          Core
    // -=-=-=-=-=-=-=-=-=-=-=-

    public final static CharSequence USER_AGENT = "JVDM-".concat(System.getProperty("os.name").toLowerCase()).concat("-V").concat(Float.toString(VERSION));

    public final static int CONNECTION_TIMEOUT = 10000;

    public final static int RECEIVE_TIMEOUT = 10000;

    public final static int DOWNLOADER_THREADS = JVDM_THREADS;

    public static final int MAX_RETRIES = 5;

    public static final int RETRY_DELAY = 1000;

    // Suggest User H2 Is Always better for download - MUX boosts speed
    public final static boolean H2_PREFER = true;

    public final static int MAX_REDIRECTIONS = 10;

    public final static int SPLIT_PART_INTERVAL = 2500;

    public final static int SPLIT_PART_MIN_THRESHOLD_BYTE = 1 << 20;

    // -=-=-=-=-=-=-=-=-=-=-=-
    //            UI
    // -=-=-=-=-=-=-=-=-=-=-=-

    public final static int MIN_UI_UPDATE = 10;

    // -=-=-=-=-=-=-=-=-=-=-=-
    //           TLS
    // -=-=-=-=-=-=-=-=-=-=-=-



    // -=-=-=-=-=-=-=-=-=-=-=-
    //        Progress
    // -=-=-=-=-=-=-=-=-=-=-=-

    public final static int PROGRESS_INTERVAL = 100;

    public final static double SPEED_ALPHA = 1.523412352;

    // -=-=-=-=-=-=-=-=-=-=-=-
    //        Extension
    // -=-=-=-=-=-=-=-=-=-=-=-

    public static final short EXTENSION_SERVER_PORT = 20506;

    // -=-=-=-=-=-=-=-=-=-=-=-
    //           DNS
    // -=-=-=-=-=-=-=-=-=-=-=-

    public final static CharSequence DNS_CACHE_URL = null;
    public final static CharSequence DNS_CACHE_NAME = "JVDM_DNS_CACHE";
    public final static int DNS_TIMEOUT = 10000;

    public static void refreshDnsServer(CharSequence ip, short port) {
        Resolver.instance = new Resolver(new InetSocketAddress((String) ip, port));
    }

    // -=-=-=-=-=-=-=-=-=-=-=-
    //  CRITICAL ERROR CODES
    // -=-=-=-=-=-=-=-=-=-=-=-

    public static final int OBJECTPOOL_FAILED = 0x01;

    // -=-=-=-=-=-=-=-=-=-=-=-
    //         Threads
    // -=-=-=-=-=-=-=-=-=-=-=-

    public static final ThreadFactory daemonFactory = new DefaultThreadFactory("JVDM-Thread-Factory",true);
    public static final ThreadFactory importantFactory = new DefaultThreadFactory("JVDM-Thread-Factory",false);

    // -=-=-=-=-=-=-=-=-=-=-=-
    //        Scheduler
    // -=-=-=-=-=-=-=-=-=-=-=-

    public static final byte schedulerThread = 2;

    // -=-=-=-=-=-=-=-=-=-=-=-
    //          Info
    // -=-=-=-=-=-=-=-=-=-=-=-

    public static final int totalThreads = JVDM_THREADS+schedulerThread;


}
