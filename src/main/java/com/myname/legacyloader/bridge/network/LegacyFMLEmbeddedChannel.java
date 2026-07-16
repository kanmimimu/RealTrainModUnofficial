package com.myname.legacyloader.bridge.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;

public class LegacyFMLEmbeddedChannel extends EmbeddedChannel {
    public LegacyFMLEmbeddedChannel(ChannelHandler... handlers) {
        super(handlers == null ? new ChannelHandler[0] : handlers);
    }
}
