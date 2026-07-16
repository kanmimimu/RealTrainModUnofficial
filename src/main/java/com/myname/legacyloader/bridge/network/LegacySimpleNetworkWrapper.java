package com.myname.legacyloader.bridge.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class LegacySimpleNetworkWrapper {
    private final String channelName;
    private final Map<Class<?>, Integer> messageIds = new HashMap<>();

    public LegacySimpleNetworkWrapper(String channelName) {
        this.channelName = channelName;
    }

    public <REQ extends LegacyMessage, REPLY extends LegacyMessage> void registerMessage(
            Class<? extends LegacyMessageHandler<REQ, REPLY>> handler,
            Class<REQ> messageType,
            int discriminator,
            LegacySide side) {
        messageIds.put(messageType, discriminator);
    }

    public void registerMessage(Class<?> handler, Class<?> messageType, int discriminator, Object side) {
        messageIds.put(messageType, discriminator);
    }

    public void sendToServer(LegacyMessage message) {
    }

    public void sendTo(LegacyMessage message, ServerPlayer player) {
    }

    public void sendToAll(LegacyMessage message) {
    }

    public void sendToAllAround(LegacyMessage message, Object targetPoint) {
    }

    public void sendToDimension(LegacyMessage message, int dimensionId) {
    }

    public String getChannelName() {
        return channelName;
    }

    public static class TargetPoint {
        public final int dimension;
        public final double x;
        public final double y;
        public final double z;
        public final double range;

        public TargetPoint(int dimension, double x, double y, double z, double range) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.range = range;
        }
    }
}
