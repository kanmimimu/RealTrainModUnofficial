package com.myname.legacyloader.bridge.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LegacyFMLProxyPacket implements LegacyMinecraftPacket {
    private final ByteBuf payload;
    private final String channel;
    private Object dispatcher;

    public LegacyFMLProxyPacket(ByteBuf payload, String channel) {
        this.payload = payload == null ? Unpooled.buffer() : payload;
        this.channel = channel == null ? "" : channel;
    }

    public ByteBuf payload() {
        return payload;
    }

    public String channel() {
        return channel;
    }

    public void setDispatcher(Object dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Object getDispatcher() {
        return dispatcher;
    }
}
