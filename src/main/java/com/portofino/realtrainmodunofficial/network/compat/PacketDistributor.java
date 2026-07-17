package com.portofino.realtrainmodunofficial.network.compat;

import com.portofino.realtrainmodunofficial.network.RealTrainModUnofficialNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Forge 1.20.1 shim exposing the subset of NeoForge's {@code PacketDistributor} static send helpers
 * used by RTMU. Delegates to the mod's {@link RealTrainModUnofficialNetwork#CHANNEL SimpleChannel}.
 */
public final class PacketDistributor {
    private PacketDistributor() {
    }

    public static void sendToServer(CustomPacketPayload payload) {
        RealTrainModUnofficialNetwork.CHANNEL.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        RealTrainModUnofficialNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        RealTrainModUnofficialNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity), payload);
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        RealTrainModUnofficialNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }
}
