package org.Kroj.Core.Network.DNS;

import org.Kroj.Core.Scheduler.Scheduler;
import org.Kroj.Core.Scheduler.Task.Task;
import org.Kroj.Core.Statics.Initializer;
import org.Kroj.Core.Tools.FileManagement.RafInputStream;
import org.Kroj.Core.Tools.FileManagement.RafOutputStream;
import org.Kroj.Core.Tools.TestUnit.Tester;
import org.msgpack.core.MessageInsufficientBufferException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.*;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class DNS {

    private final Map<CharSequence, InetAddress> db = new ConcurrentHashMap<>();
    private final Map<CharSequence, CompletableFuture<InetAddress>> pending = new ConcurrentHashMap<>();
    private final RafInputStream input;
    private final RafOutputStream output;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private boolean edited = false;

    private static final DNS instance;

    static {
        String ur = System.getProperty("java.io.tmpdir")+File.separator+Initializer.DNS_CACHE_NAME;
        instance = new DNS((Initializer.DNS_CACHE_URL == null ? ur : Initializer.DNS_CACHE_URL).toString());
    }

    public static DNS getInstance() {
        return instance;
    }

    public DNS(File file) throws IOException {
        this(file.getPath());
    }
    public DNS(String filePath) {
        this.input = new RafInputStream(filePath);
        this.output = new RafOutputStream(filePath);

        load();

        if (!output.isError()) {
            Scheduler.getInstance().executeLoop(new Task() {
                @Override
                public CharSequence getID() {
                    return "DNS-C";
                }

                @Override
                public CharSequence getName() {
                    return "Dns Cache Save";
                }

                @Override
                public void execute() throws Exception {
                    instance.saveAndFlush();
                }

                @Override
                public void onException(Exception e) {
                    logger.error().append(e).nextLine();
                }
            },10,TimeUnit.MINUTES);
        }
    }

    // Max Speed
    private void load() {
        if (input.isError()) {
            logger.error().append("File does not exist, Could not load DNS Cache").nextLine();
            return;
        }
        try {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(input);
            logger.info().append("Loading DNS Cache").nextLine();
            int hCount = unpacker.unpackArrayHeader();

            logger.info().append("Found ").append(String.valueOf(hCount)).append(" DNS from File").nextLine();

            for (int i = 0; i < hCount; i++) {
                String host = unpacker.unpackString();
                String ip = unpacker.unpackString();
                db.put(host, InetAddress.getByName(ip));
                logger.debug().append("Loaded: ").append(host).append(":").append(ip).nextLine();
            }

            logger.info().append("Loaded DNS Cache").nextLine();

        } catch (MessageInsufficientBufferException _) {
            logger.info().append("File Is Empty, Its okay").nextLine();
        } catch (IOException e) {
            logger.error().append("Make Sure App have permission to read specified file :(").nextLine();
        }
    }

    // 7-12 NS For 1M DNS
    public InetAddress resolve(CharSequence host) {
        InetAddress result = db.get(host);
        if (result != null) return result;

        CompletableFuture<InetAddress> future = pending.computeIfAbsent(host,
                k -> CompletableFuture.supplyAsync(() -> {
                    try {
                        InetAddress addr = Resolver.instance.query(host)
                                .get(Initializer.DNS_TIMEOUT, TimeUnit.MILLISECONDS);
                        db.put(host, addr);
                        edited = true;
//                        logger.info().append("Resolved ").append(host).append(":").append(addr).nextLine();
                        return addr;
                    } catch (InterruptedException e) {
                        logger.error().append("Failed To Resolve Host, Interrupted").nextLine();
                        return null;
                    } catch (ExecutionException e) {
                        logger.error().append("Execution To Resolve Hostname Failed!").nextLine();
                        return null;
                    } catch (TimeoutException e) {
                        logger.error().append("Could Not Resolved Host Because DNS Timed Out").nextLine();
                        logger.info().append("Using Next DNS Server").nextLine();
                        return null;
                    }
                }, executor).whenComplete((_,_) -> pending.remove(host)));
        try {
            result = future.get(Initializer.DNS_TIMEOUT, TimeUnit.MILLISECONDS);
            return result;
        } catch (InterruptedException e) {
            logger.error().append("Failed To Resolve Host, Interruption Failed").nextLine();
            // TODO think about this
            return null;
        } catch (ExecutionException e) {
            logger.error().append("Execution To Resolve Hostname Failed!").nextLine();
            // TODO Find Problem And Try Again
            return null;
        } catch (TimeoutException e) {
            logger.error().append("Could Not Resolved Host Because DNS Timed Out").nextLine();
            logger.info().append("Using Next DNS Server");
            //TODO Next DNS Server
            return null;
        }
    }

    public void renew(CharSequence host) {
        try {
            db.put(host, Resolver.instance.query(host).get(Initializer.DNS_TIMEOUT,TimeUnit.MILLISECONDS));
            edited = true;
        } catch (InterruptedException e) {
            logger.error().append("Failed To Resolve Host, Interruption Failed").nextLine();
        } catch (ExecutionException e) {
            logger.error().append("Execution To Resolve Hostname Failed!").nextLine();
            //TODO retry again and find the problem
        } catch (TimeoutException e) {
            logger.error().append("Could Not Resolved Host Because DNS Timed Out").nextLine();
            logger.info().append("Using Next DNS Server");
            //TODO Go To next order of dns lists
        }
    }

    public void forceCache(CharSequence host, InetAddress address) {
        db.put(host, address);
        edited = true;
    }

    public void saveAndFlush() {
        if (db.isEmpty()) {
            logger.debug().append("Dns Cache was Empty").nextLine();
            return;
        }
        if (!edited) return;
        edited = false;
        //TODO Binary Save/Read
        try {
            MessagePacker packer = MessagePack.newDefaultPacker(output);
            packer.packArrayHeader(db.size());

            for (Map.Entry<CharSequence,InetAddress> entry : db.entrySet()) {
                packer.packString(entry.getKey().toString());
                packer.packString(entry.getValue().getHostAddress());
            }

            packer.flush();
            packer.close();

            logger.info().append("Cache Saved Successfully!").nextLine();

            output.returnToStart();

        } catch (IOException e) {
            logger.error().append("Make Sure App have permission to write on specified folder :(").nextLine();
        }
    }

    public void finish() {
        saveAndFlush();
        if (!input.isError()) {
            try {
                input.close();
            } catch (IOException e) {
                logger.error().append("Error Closing Input Stream!").nextLine();
            }
        }
        if (!output.isError()) {
            try {
                output.close();
            } catch (IOException e) {
                logger.error().append("Error Closing Output Stream!").nextLine();
            }
        }
        executor.shutdown();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1_000_000; i++) {
            getInstance().forceCache(""+i, InetAddress.getLoopbackAddress());
        }
        Tester.speedTest(100,() -> getInstance().saveAndFlush());
        DNS.getInstance().finish();
    }
}
