package com.portofino.realtrainmodunofficial.network.compat;

import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Forge 1.20.1 shim for NeoForge's {@code IPayloadContext}. Only {@link #player()} and
 * {@link #enqueueWork(Runnable)} are used by RTMU handlers.
 *
 * <p>{@link #player()} returns the sending {@code ServerPlayer} (null on the client). RTMU's
 * client-bound handlers never call it, so this stays common code with no {@code net.minecraft.client}
 * dependency.
 */
public interface IPayloadContext {
    Player player();

    CompletableFuture<Void> enqueueWork(Runnable work);
}
