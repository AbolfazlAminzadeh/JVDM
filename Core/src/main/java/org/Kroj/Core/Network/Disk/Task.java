package org.Kroj.Core.Network.Disk;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;


public record Task(ByteBuf buffer, long pos, Channel channel) {}
