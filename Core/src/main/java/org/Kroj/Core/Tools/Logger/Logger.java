package org.Kroj.Core.Tools.Logger;

import org.Kroj.Core.Tools.ObjectManagement.ObjectsPool;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger {

    public static final Logger logger = new Logger(System.out);

    private final ArrayList<BufferedOutputStream> out = new ArrayList<>();
    private final ThreadLocal<StringBuilder> builders = ThreadLocal.withInitial(ObjectsPool.stringBuilders::get);

    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();

    private LOG_LEVEL ll = LOG_LEVEL.INFO;

    private volatile boolean running = true;

    private volatile boolean showPrefix = false;
    private volatile boolean showTime = false;

    public Logger(OutputStream... out) {
        this.out.addAll(Arrays.stream(out).map(BufferedOutputStream::new).toList());
        Thread loggerThread = new Thread(this::loggerLoop, "LoggerThread");
        loggerThread.setDaemon(false);
        loggerThread.start();
    }

    private void loggerLoop() {
        try {
            while (running) {
                String msg = logs.poll();
                if (msg == null) {
                    Thread.sleep(5);
                    continue;
                }
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                for (BufferedOutputStream out : out) {
                    out.write(bytes);
                    out.flush();
                }
            }
        } catch (Exception _) {}
    }

    private void checkTime() {
        if (showTime) {
            builders.get().append('[').append(System.nanoTime()).append(']').append(' ');
        }
    }
    private void checkPrefix() {
        if (showPrefix) {
            builders.get().append('[').append(ll.name().toUpperCase()).append(']').append(' ');
        }
        checkTime();
    }

    public Logger append(String str) {
        builders.get().append(str);
        return this;
    }

    public Logger append(char c) {
        builders.get().append(c);
        return this;
    }

    public Logger append(int i) {
        builders.get().append(i);
        return this;
    }

    public Logger append(long i) {
        builders.get().append(i);
        return this;
    }

    public Logger append(float f) {
        builders.get().append(f);
        return this;
    }

    public Logger append(double d) {
        builders.get().append(d);
        return this;
    }

    public Logger append(byte[] bytes) {
        builders.get().append(new LowString(bytes));
        return this;
    }

    public Logger append(Throwable throwable) {
        StringBuilder builder = builders.get();
        builder.append(throwable.getClass());
        if (throwable.getMessage() != null) {
            builder.append(throwable.getMessage());
        }
        if (throwable.getStackTrace() != null) {
            for (StackTraceElement element : throwable.getStackTrace()) {
                builder.append(element).append(System.lineSeparator());
            }
        }
        return this;
    }

    public Logger append(Object[] objs) {
        append('[');
        for (int i = 0; i < objs.length-1; i++) {
            append(objs[i]).append(',').append(' ');
        }
        append(objs[objs.length-1]).append(']');
        return this;
    }

    public Logger append(Object obj) {
        builders.get().append(obj);
        return this;
    }

    public Logger nextLine() {
        StringBuilder sb = builders.get();
        sb.append(System.lineSeparator());
        logs.offer(sb.toString());
        sb.setLength(0);
        checkPrefix();
        return this;
    }

    public void flush() {
        StringBuilder sb = builders.get();
        logs.offer(sb.toString());
        sb.setLength(0);
    }

    private Logger setLogLevel(LOG_LEVEL log_level) {
        this.ll = log_level;return this;
    }

    public Logger warn() {return setLogLevel(LOG_LEVEL.WARN);}
    public Logger info() {return setLogLevel(LOG_LEVEL.INFO);}
    public Logger error() {return setLogLevel(LOG_LEVEL.ERROR);}
    public Logger debug() {return setLogLevel(LOG_LEVEL.DEBUG);}

    public void addOutputStreams(OutputStream... pws) {
        out.addAll(Arrays.stream(pws).map(BufferedOutputStream::new).toList());
    }

    public void setShowPrefix(boolean showPrefix) {
        this.showPrefix = showPrefix;
    }
    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public void close() {
        running = false;
        out.forEach(o -> {try {o.close();} catch (Exception e) {System.out.println(e.getMessage());}});
        out.clear();
    }

}
