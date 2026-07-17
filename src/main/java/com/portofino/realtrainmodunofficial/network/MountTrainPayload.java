package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficialItems;
import com.portofino.realtrainmodunofficial.entity.TrainEntity;
import com.portofino.realtrainmodunofficial.network.compat.CustomPacketPayload;
import com.portofino.realtrainmodunofficial.network.compat.IPayloadContext;
import com.portofino.realtrainmodunofficial.network.compat.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record MountTrainPayload() implements CustomPacketPayload {
    public static final MountTrainPayload INSTANCE = new MountTrainPayload();
    public static final Type<MountTrainPayload> TYPE = new Type<>(
        new ResourceLocation(RealTrainModUnofficial.MODID, "mount_train")
    );
    public static final StreamCodec<ByteBuf, MountTrainPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(MountTrainPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                boolean holdingCrowbar = player.getMainHandItem().is(RealTrainModUnofficialItems.CROWBAR_ITEM.get())
                    || player.getOffhandItem().is(RealTrainModUnofficialItems.CROWBAR_ITEM.get());
                if (holdingCrowbar) {
                    TrainEntity.tryEnterCouplingModeFromPlayerView(player);
                } else {
                    TrainEntity.tryRideFromPlayerView(player);
                }
            }
        });
    }
}
