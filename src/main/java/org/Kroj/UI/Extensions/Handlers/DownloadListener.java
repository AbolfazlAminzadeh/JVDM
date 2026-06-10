package org.Kroj.UI.Extensions.Handlers;

import com.sun.tools.javac.Main;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.Kroj.Core.Network.Download.Manager;
import org.Kroj.Core.Network.SocketBind.BindToDeviceHandler;
import org.Kroj.Core.Tools.String.SizeManager;
import org.Kroj.UI.JavaFX.App;

import static org.Kroj.Core.Tools.Logger.Logger.logger;

public class DownloadListener extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        if (o instanceof EndpointData data) {
            logger.append(data).nextLine().log();
            App.getInstance().getController().addDownloadLink(data.getUrl());
        } else if (o instanceof PingWebSocketFrame) {
            ctx.write(new PongWebSocketFrame());
        } else if (o instanceof PongWebSocketFrame) {
            ctx.write(new PongWebSocketFrame());
        } else if (o instanceof CloseWebSocketFrame) {
            ctx.write(new CloseWebSocketFrame());
            ctx.close();
        }
    }
}
