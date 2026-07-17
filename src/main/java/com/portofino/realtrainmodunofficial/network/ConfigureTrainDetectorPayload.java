package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.blockentity.TrainDetectorBlockEntity;
import com.portofino.realtrainmodunofficial.network.compat.ByteBufCodecs;
import com.portofino.realtrainmodunofficial.network.compat.CustomPacketPayload;
import com.portofino.realtrainmodunofficial.network.compat.IPayloadContext;
import com.portofino.realtrainmodunofficial.network.compat.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ConfigureTrainDetectorPayload(BlockPos pos, int channel, int range) implements CustomPacketPayload {
    public static final Type<ConfigureTrainDetectorPayload> TYPE = new Type<>(
        new ResourceLocation(RealTrainModUnofficial.MODID, "configure_train_detector")
    );

    public static final StreamCodec<ByteBuf, ConfigureTrainDetectorPayload> STREAM_CODEC = StreamCodec.composite(
        com.portofino.realtrainmodunofficial.network.compat.ByteBufCodecs.BLOCK_POS,
        ConfigureTrainDetectorPayload::pos,
        ByteBufCodecs.INT,
        ConfigureTrainDetectorPayload::channel,
        ByteBufCodecs.INT,
        ConfigureTrainDetectorPayload::range,
        ConfigureTrainDetectorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(ConfigureTrainDetectorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof TrainDetectorBlockEntity blockEntity)) {
                return;
            }
            blockEntity.configure(payload.channel(), payload.range());
            player.level().sendBlockUpdated(payload.pos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            player.displayClientMessage(Component.literal("電車検知ブロックを更新しました"), true);
        });
    }
}
