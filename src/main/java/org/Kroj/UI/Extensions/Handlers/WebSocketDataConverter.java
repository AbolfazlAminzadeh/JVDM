package org.Kroj.UI.Extensions.Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.util.List;

import static org.Kroj.Core.Tools.Logger.Logger.logger;


public class WebSocketDataConverter extends MessageToMessageDecoder<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error().append(cause.getMessage()).nextLine().append(cause.getStackTrace()).nextLine().log();
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Object o, List<Object> list) throws Exception {
        if (o instanceof BinaryWebSocketFrame data) {
            ByteBuf buf = data.content();
            byte[] bytes = new byte[buf.readableBytes()];
            data.content().readBytes(bytes);
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
            int count = unpacker.unpackArrayHeader();
            for (int i = 0;  i < count; i++) {
                unpacker.unpackArrayHeader();
                byte status = unpacker.unpackByte();
                String url = unpacker.unpackString();
                list.add(new EndpointData(status, url));
            }
            unpacker.close();
        }
    }
}
