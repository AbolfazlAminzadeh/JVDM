package org.Kroj.Extensions;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.Kroj.Extensions.Handlers.DownloadListener;
import org.Kroj.Extensions.Handlers.WebSocketDataConverter;
import org.Kroj.Core.Network.Netty.NettyUtil;
import org.Kroj.Core.Statics.Initializer;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class Server {

    private final int port;

    final MultiThreadIoEventLoopGroup bossGroup;
    final ServerBootstrap bootstrap;

    public static final Server instance = new Server(Initializer.EXTENSION_SERVER_PORT);

    ChannelFuture channelFuture;

    public Server(int port) {

        this.port = port;
        bossGroup = NettyUtil.getEventLoopGroup();
        MultiThreadIoEventLoopGroup workerGroup = NettyUtil.getEventLoopGroup();

        bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NettyUtil.getServerTCPClass())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1<<16)) // 2^16 = 64KB
                                .addLast(new WebSocketServerProtocolHandler("/ws",null,true,1<<24)) // 2 ^ 24 = 16MB
                                .addLast(new WebSocketDataConverter())
                                .addLast(new DownloadListener());
                    }
                });
    }

    public void start() {
        try {
            logger.debug().append("Starting server on port " + port).nextLine();
            channelFuture = bootstrap.bind(port).sync();
            logger.info().append("Server started. Listening on port " + port).nextLine();
        } catch (InterruptedException e) {
            logger.error().append("Interrupted for starting server, retry again please").nextLine().debug().append(e.getMessage()).nextLine();
        }
    }

    public void stop() {
        if (channelFuture != null) {
            channelFuture.channel().close();
        } else {
            logger.warn().append("Server Is Already Stopped").nextLine();
        }
    }

    public void close() {
        stop();
        bossGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        Server.instance.start();
    }

}