package com.myname.legacyloader.bridge.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class LegacyMessageContext {
    private final LegacySide side;
    private ServerPlayer serverPlayer;
    private Supplier<?> contextSupplier;

    public LegacyMessageContext(LegacySide side) {
        this.side = side;
    }

    public LegacyMessageContext(Supplier<?> contextSupplier) {
        this.contextSupplier = contextSupplier;
        this.side = LegacySide.SERVER;
    }

    public LegacySide side() {
        return side;
    }

    public LegacySide getSide() {
        return side;
    }

    public ServerPlayer getServerHandler() {
        return serverPlayer;
    }

    public Object getClientHandler() {
        return null;
    }

    public void setPacketHandled() {
    }
}
