package org.kroj.Core.Network.DNS;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;

import static org.kroj.Core.Tools.Logger.Logger.logger;

public class ResolverHandler extends SimpleChannelInboundHandler<DatagramDnsResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) throws Exception {
        Promise<InetAddress> pending = Resolver.instance.queries.remove(msg.id());
        if (pending == null) return;
        final int l = msg.count(DnsSection.ANSWER);
        for (int i = 0;i<l;i++) {
            DnsRecord rec = msg.recordAt(DnsSection.ANSWER, i);
            if (rec instanceof DnsRawRecord raw && raw.type() == DnsRecordType.A) {
                final ByteBuf rawContent = raw.content();
                if (rawContent.isReadable() && rawContent.readableBytes() == 4) {
                    byte[] bytes = new byte[rawContent.readableBytes()];
                    rawContent.readBytes(bytes);
                    InetAddress address = InetAddress.getByAddress(bytes);
                    pending.trySuccess(address);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error().append(cause).nextLine();
    }
}
