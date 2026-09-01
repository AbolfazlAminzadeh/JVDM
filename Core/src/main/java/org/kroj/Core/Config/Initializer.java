package org.kroj.Core.Config;


import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.File;
import java.util.concurrent.ThreadFactory;

public final class Initializer {

    // -=-=-=-=-=-=-=-=-=-=-=-
    //          JVDM
    // -=-=-=-=-=-=-=-=-=-=-=-

    public final static float VERSION = 1.3F;

    public final static int CPU_THREADS = Runtime.getRuntime().availableProcessors();

    public final static int JVDM_THREADS = CPU_THREADS * 2;

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

    public final static int MINIMUM_BUFFER_SIZE = 1 << 16;

    public final static int INITIAL_BUFFER_SIZE = 1 << 20;

    public final static int MAXIMUM_BUFFER_SIZE = 1 << 23;

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
    //          Disk
    // -=-=-=-=-=-=-=-=-=-=-=-

    public static final int DISK_QUEUE_CAPACITY = 1 << 12;

    public static final int DISK_QUEUE_PAUSE_READ = 1 << 10;

    public static final int DISK_QUEUE_RESUME_READ = DISK_QUEUE_PAUSE_READ << 1 / 3; // Will change later

    // -=-=-=-=-=-=-=-=-=-=-=-
    //          Info
    // -=-=-=-=-=-=-=-=-=-=-=-

    public static final int totalThreads = JVDM_THREADS+schedulerThread;

}