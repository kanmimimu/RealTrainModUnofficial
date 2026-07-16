package com.myname.legacyloader.bridge.network;

import io.netty.util.AttributeKey;

public final class LegacyFMLOutboundHandler {
    public static final AttributeKey<OutboundTarget> FML_MESSAGETARGET =
            AttributeKey.valueOf("fml:messagetarget");

    private LegacyFMLOutboundHandler() {
    }

    public enum OutboundTarget {
        TOSERVER,
        PLAYER,
        ALL,
        DIMENSION,
        ALLAROUND,
        NOARGS
    }
}
