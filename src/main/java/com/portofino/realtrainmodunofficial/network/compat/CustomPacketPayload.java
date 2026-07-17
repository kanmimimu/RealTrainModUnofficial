package com.portofino.realtrainmodunofficial.network.compat;

import net.minecraft.resources.ResourceLocation;

/**
 * Forge 1.20.1 compatibility shim for the 1.20.5+/NeoForge {@code CustomPacketPayload} API.
 *
 * <p>Payload records keep their original {@code TYPE}/{@code type()} shape; the mod's SimpleChannel
 * adapter in {@link com.portofino.realtrainmodunofficial.network.RealTrainModUnofficialNetwork}
 * drives encode/decode/handle. The {@code Type} id is retained for readability but Forge discriminates
 * messages by their concrete class, not the {@link ResourceLocation}.
 */
public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }
}
