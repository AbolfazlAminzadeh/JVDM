package org.Kroj.Core.Network.DNS;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.util.concurrent.Promise;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Statics.Initializer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class Resolver {

    private final EventLoopGroup group;

    private Channel channel;

    public static Resolver instance = new Resolver(
            new InetSocketAddress("8.8.8.8", 53),
            new InetSocketAddress("8.8.4.4", 53),
            new InetSocketAddress("1.1.1.1", 53),
            new InetSocketAddress("1.0.0.1", 53),
            new InetSocketAddress("85.15.1.14", 53),
            new InetSocketAddress("85.15.1.15", 53)
    );

    final List<InetSocketAddress> addresses;
    final ConcurrentHashMap<Integer, Promise<InetAddress>> queries = new ConcurrentHashMap<>();

    public Resolver(InetSocketAddress... servers) {
        addresses = Arrays.asList(servers);
        this.group = NettyUtil.getEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {

            bootstrap.group(group).channel(NettyUtil.getUDPClass())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_RCVBUF, 1 << 20)
                    .option(ChannelOption.SO_SNDBUF, 1 << 20)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        public void initChannel(DatagramChannel ch) {
                            ch.pipeline()
                                    .addLast(new DatagramDnsResponseDecoder())
                                    .addLast(new DatagramDnsQueryEncoder())
                                    .addLast(new ResolverHandler())
                            ;
                        }
                    });
            channel = bootstrap.bind(0).sync().channel();
        } catch (InterruptedException e) {
            logger.error().append(e);
        } finally {
            logger.nextLine();
        }
    }

    // 24MS - Shatel - UDP
    public Promise<InetAddress> query(CharSequence host) {
        try {
            EventLoop loop = group.next();
            Promise<InetAddress> promise = loop.newPromise();


            for (InetSocketAddress address : addresses) {

                int id = ThreadLocalRandom.current().nextInt(0,1<<16);

                DatagramDnsQuery query = new DatagramDnsQuery(null, address,id);

                queries.put(id, promise);

                query.setRecursionDesired(true);

                query.addRecord(DnsSection.QUESTION, new DefaultDnsQuestion(host.toString(),DnsRecordType.A));

                channel.write(query);

            }

            channel.flush();

            promise.await(Initializer.DNS_TIMEOUT, TimeUnit.MILLISECONDS);

            return promise;
        } catch (InterruptedException e) {
            logger.error().append(e.getMessage()).nextLine();
        } catch (NullPointerException _) {}
        return null;
    }

    public void close() throws InterruptedException {
        channel.close().sync();
    }

    public static void main(String[] args) {
        Resolver res = Resolver.instance;
        res.query("google.com");
        logger.append(res.query("soft98.ir").getNow().getHostAddress()).nextLine();
//        Tester.speedTest(100,() -> res.query("soft98.ir"));
    }

}