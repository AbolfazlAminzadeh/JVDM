package org.Kroj.Core.Network.Netty;

import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.*;
import io.netty.channel.kqueue.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.Kroj.Core.Statics.Initializer;

public class NettyUtil {

    public static MultiThreadIoEventLoopGroup getEventLoopGroup() {
        return new MultiThreadIoEventLoopGroup(
                Epoll.isAvailable() ?
                        EpollIoHandler.newFactory() :
                        KQueue.isAvailable() ?
                                KQueueIoHandler.newFactory() :
                                NioIoHandler.newFactory()
        );
    }

    public static MultiThreadIoEventLoopGroup getEventLoopGroup(int maxThreads) {
        return new MultiThreadIoEventLoopGroup(
                maxThreads,
                Initializer.daemonFactory,
                Epoll.isAvailable() ?
                        EpollIoHandler.newFactory() :
                        KQueue.isAvailable() ?
                                KQueueIoHandler.newFactory() :
                                NioIoHandler.newFactory()
        );
    }

    public static Class<? extends SocketChannel> getTCPClass() {
        return Epoll.isAvailable() ?
                EpollSocketChannel.class :
                KQueue.isAvailable() ?
                        KQueueSocketChannel.class :
                        NioSocketChannel.class;
    }

    public static Class<? extends DatagramChannel> getUDPClass() {
        return Epoll.isAvailable() ?
                EpollDatagramChannel.class :
                KQueue.isAvailable() ?
                        KQueueDatagramChannel.class :
                        NioDatagramChannel.class;
    }

    public static Class<? extends ServerChannel> getServerTCPClass() {
        return Epoll.isAvailable() ?
                EpollServerSocketChannel.class :
                KQueue.isAvailable() ?
                        KQueueServerSocketChannel.class :
                        NioServerSocketChannel.class;
    }
}