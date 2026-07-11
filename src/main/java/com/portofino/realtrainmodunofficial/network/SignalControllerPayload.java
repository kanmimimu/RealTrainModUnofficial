package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import jp.masa.signalcontrollermod.SignalType;
import jp.masa.signalcontrollermod.TileEntitySignalController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * SignalControllerMod 移植: 設定 GUI → サーバー反映 (原作 PacketSignalController 相当)。
 * action: 0=設定一括 (signalType/above/last/repeat/reducedSpeed),
 *         1=nextSignal クリア, 2=displayPos クリア
 */
public record SignalControllerPayload(BlockPos pos, int action, String signalType,
                                      boolean above, boolean last, boolean repeat, boolean reducedSpeed)
        implements CustomPacketPayload {

    public static final Type<SignalControllerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RealTrainModUnofficial.MODID, "signal_controller"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignalControllerPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos());
                buf.writeVarInt(p.action());
                buf.writeUtf(p.signalType());
                int flags = (p.above() ? 1 : 0) | (p.last() ? 2 : 0) | (p.repeat() ? 4 : 0) | (p.reducedSpeed() ? 8 : 0);
                buf.writeByte(flags);
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                int action = buf.readVarInt();
                String type = buf.readUtf();
                int flags = buf.readByte();
                return new SignalControllerPayload(pos, action, type,
                        (flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0, (flags & 8) != 0);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(SignalControllerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.distanceToSqr(payload.pos().getX(), payload.pos().getY(), payload.pos().getZ()) > 64.0D * 64.0D) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof TileEntitySignalController controller)) {
                return;
            }
            switch (payload.action()) {
                case 1 -> controller.getNextSignal().clear();
                case 2 -> controller.getDisplayPos().clear();
                default -> {
                    controller.setSignalType(SignalType.getType(payload.signalType()));
                    controller.setAbove(payload.above());
                    controller.setLast(payload.last());
                    controller.setRepeat(payload.repeat());
                    controller.setReducedSpeed(payload.reducedSpeed());
                }
            }
            controller.syncToClient();
        });
    }
}
