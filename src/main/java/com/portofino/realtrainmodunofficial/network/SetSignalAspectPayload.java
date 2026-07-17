package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.blockentity.SignalRemoteBlockEntity;
import com.portofino.realtrainmodunofficial.network.compat.ByteBufCodecs;
import com.portofino.realtrainmodunofficial.network.compat.CustomPacketPayload;
import com.portofino.realtrainmodunofficial.network.compat.IPayloadContext;
import com.portofino.realtrainmodunofficial.network.compat.StreamCodec;
import com.portofino.realtrainmodunofficial.signal.SignalAspect;
import com.portofino.realtrainmodunofficial.signal.SignalNetworkSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record SetSignalAspectPayload(BlockPos pos, int aspectId) implements CustomPacketPayload {
    public static final Type<SetSignalAspectPayload> TYPE = new Type<>(
        new ResourceLocation(RealTrainModUnofficial.MODID, "set_signal_aspect")
    );

    public static final StreamCodec<ByteBuf, SetSignalAspectPayload> STREAM_CODEC = StreamCodec.composite(
        com.portofino.realtrainmodunofficial.network.compat.ByteBufCodecs.BLOCK_POS,
        SetSignalAspectPayload::pos,
        ByteBufCodecs.INT,
        SetSignalAspectPayload::aspectId,
        SetSignalAspectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(SetSignalAspectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof SignalRemoteBlockEntity blockEntity)) {
                return;
            }
            int linkedChannel = blockEntity.getLinkedChannel();
            if (linkedChannel <= 0) {
                player.displayClientMessage(Component.literal("この変更機はまだ信号に接続されていません"), true);
                return;
            }
            SignalNetworkSavedData data = SignalNetworkSavedData.get(player.serverLevel());
            if (!data.hasChannel(linkedChannel)) {
                player.displayClientMessage(Component.literal("信号番号が無効になっています"), true);
                return;
            }
            SignalAspect aspect = SignalAspect.byId(payload.aspectId());
            data.setAspect(player.serverLevel().getServer(), linkedChannel, aspect);
            player.displayClientMessage(Component.literal(aspect.getLabel() + " に変更しました"), true);
        });
    }
}
