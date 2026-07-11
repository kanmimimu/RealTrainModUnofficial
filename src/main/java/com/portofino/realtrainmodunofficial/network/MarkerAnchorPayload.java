package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import jp.ngt.rtm.rail.BlockMarker;
import jp.ngt.rtm.rail.TileEntityMarker;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 本家 PacketMarkerRPClient 相当: レンチのアンカー移動 (レール形状編集) で
 * クライアントが編集した RailPosition をサーバーへ反映する。
 */
public record MarkerAnchorPayload(BlockPos markerPos, CompoundTag railPositionTag) implements CustomPacketPayload {

    public static final Type<MarkerAnchorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RealTrainModUnofficial.MODID, "marker_anchor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkerAnchorPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MarkerAnchorPayload::markerPos,
            net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG,
            MarkerAnchorPayload::railPositionTag,
            MarkerAnchorPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(MarkerAnchorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            //編集対象マーカーが近距離にあることを確認 (不正パケット対策)
            if (player.distanceToSqr(payload.markerPos().getX(), payload.markerPos().getY(), payload.markerPos().getZ()) > 256.0D * 256.0D) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.markerPos()) instanceof TileEntityMarker marker)) {
                return;
            }
            RailPosition rp = RailPosition.readFromNBT(payload.railPositionTag());
            marker.setMarkerRP(rp);
            marker.setChanged();
            //プレビュー再構築 (本家: makeRailMap)
            if (player.level().getBlockState(payload.markerPos()).getBlock() instanceof BlockMarker block) {
                block.makeRailMap(marker, payload.markerPos().getX(), payload.markerPos().getY(), payload.markerPos().getZ(), player);
            }
        });
    }
}
