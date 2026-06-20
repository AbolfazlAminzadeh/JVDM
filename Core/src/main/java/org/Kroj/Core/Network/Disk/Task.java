package org.Kroj.Core.Network.Disk;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.Kroj.Core.Network.Download.Part.Part;


public record Task(ByteBuf buffer, long pos, Channel channel, Part part) {}