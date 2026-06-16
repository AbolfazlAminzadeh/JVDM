package org.Kroj.Core.Network.SocketBind;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

// TODO Caching + Performance Increase
public class BindToDeviceHandler extends ChannelDuplexHandler {

    private final String deviceName;

    static {
        if (Epoll.isAvailable()) {
            logger.debug().append("OS Is Linux, Using EPOLL :)").nextLine();
            loadSocBind();
            logger.append("SocBind Library Loaded Successfully").nextLine();
        }
    }

    public BindToDeviceHandler(String device) {
        this.deviceName = device;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {

        if (ctx.channel() instanceof EpollSocketChannel ch) {
            try {
                int fd = ch.fd().intValue();
                ChannelBinder.bindToDevice(fd, deviceName);
//                logger.debug().append("Linux/Epoll: Successfully bound FD ").append(fd).append(" to ").append(deviceName).nextLine();
            } catch (Exception e) {
                logger.error().append("Linux Native Bind Failed: ").append(e.getMessage()).nextLine();
            }
        } else {
            if (localAddress == null) {
                NetworkInterface ni = NetworkInterface.getByName(deviceName);

                if (ni != null && ni.isUp()) {
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    InetAddress targetIpv4 = null;

                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address ipv4) {
                            targetIpv4 = ipv4;
                            break;
                        }
                    }

                    if (targetIpv4 != null) {
                        localAddress = new InetSocketAddress(targetIpv4, 0);
//                        logger.debug().append("Windows/NIO: Intercepted connect. Binding to IP: ").append(targetIpv4.getHostAddress()).nextLine();
                    } else {
                        logger.warn().append("Interface ").append(deviceName).append(" has no valid IPv4.").nextLine();
                    }
                } else {
                    logger.error().append("No active device found: ").append(deviceName).nextLine();
                }
            }
        }

        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error().append(cause.getMessage()).nextLine().log();
        ctx.close();
    }

    private static void loadSocBind() {
        try {
            InputStream in = ChannelBinder.class.getResourceAsStream("/libSocBind.so");
            if (in == null) {
                logger.append("No libSocBind.so Found!");
                System.exit(1);
            }

            File tempLib = File.createTempFile("libSocBind", ".so");
            tempLib.deleteOnExit();

            Files.copy(in, tempLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            System.load(tempLib.getAbsolutePath());
        } catch (IOException e) {
            logger.append("Failed to load libSocBind.so!").nextLine();
            System.exit(1);
        }
    }
}