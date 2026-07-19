package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.RtmuSettings;
import com.portofino.realtrainmodunofficial.network.compat.CustomPacketPayload;
import com.portofino.realtrainmodunofficial.network.compat.IPayloadContext;
import com.portofino.realtrainmodunofficial.network.compat.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * クライアントの RTMU 軽量化設定 (車両描画距離/静止車両の再計算頻度/遠方省略) をサーバーへ同期する。
 * 現状これらの設定はクライアント側の描画判定のみで完結しており、サーバー側の常駐ロジックから
 * 読まれるわけではないが、移植元の RtmuSettings フレームワークと構成を揃えるため同期経路自体は
 * 用意しておく。
 */
public record RtmuSettingsPayload(int vehicleRenderDistance, int staticVehicleThrottle,
                                  boolean skipDistantVehicleExtras)
        implements CustomPacketPayload {

    public static final Type<RtmuSettingsPayload> TYPE = new Type<>(
            new ResourceLocation(RealTrainModUnofficial.MODID, "rtmu_settings"));

    public static final StreamCodec<FriendlyByteBuf, RtmuSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.vehicleRenderDistance());
                buf.writeVarInt(p.staticVehicleThrottle());
                buf.writeBoolean(p.skipDistantVehicleExtras());
            },
            buf -> new RtmuSettingsPayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(RtmuSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                RtmuSettings.setServerValues(player.getUUID(), payload.vehicleRenderDistance(),
                        payload.staticVehicleThrottle(), payload.skipDistantVehicleExtras());
            }
        });
    }
}
