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

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... others) {
        RealTrainModUnofficialNetwork.CHANNEL.sendToServer(payload);
        for (CustomPacketPayload o : others) {
            RealTrainModUnofficialNetwork.CHANNEL.sendToServer(o);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... others) {
        var target = net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player);
        RealTrainModUnofficialNetwork.CHANNEL.send(target, payload);
        for (CustomPacketPayload o : others) {
            RealTrainModUnofficialNetwork.CHANNEL.send(target, o);
        }
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... others) {
        var target = net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity);
        RealTrainModUnofficialNetwork.CHANNEL.send(target, payload);
        for (CustomPacketPayload o : others) {
            RealTrainModUnofficialNetwork.CHANNEL.send(target, o);
        }
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... others) {
        var target = net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
        RealTrainModUnofficialNetwork.CHANNEL.send(target, payload);
        for (CustomPacketPayload o : others) {
            RealTrainModUnofficialNetwork.CHANNEL.send(target, o);
        }
    }
}
