package com.portofino.realtrainmodunofficial.network;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.network.compat.CustomPacketPayload;
import com.portofino.realtrainmodunofficial.network.compat.IPayloadContext;
import com.portofino.realtrainmodunofficial.network.compat.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Central Forge {@link SimpleChannel} for the mod. Replaces the NeoForge {@code PayloadRegistrar};
 * each payload keeps its original {@code STREAM_CODEC} and {@code handleOn*} method, which this
 * adapter bridges onto {@code registerMessage}. Call {@link #register()} once from the mod constructor.
 */
public final class RealTrainModUnofficialNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RealTrainModUnofficial.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static int nextId;

    private RealTrainModUnofficialNetwork() {
    }

    public static void register() {
        reg(SelectModelPayload.class, SelectModelPayload.STREAM_CODEC, SelectModelPayload::handleOnServer);
        reg(TrainControlPayload.class, TrainControlPayload.STREAM_CODEC, TrainControlPayload::handleOnServer);
        reg(TrainSoundPayload.class, TrainSoundPayload.STREAM_CODEC, TrainSoundPayload::handleOnClient);
        reg(MountTrainPayload.class, MountTrainPayload.STREAM_CODEC, MountTrainPayload::handleOnServer);
        reg(RailPreviewAdjustPayload.class, RailPreviewAdjustPayload.STREAM_CODEC, RailPreviewAdjustPayload::handleOnServer);
        reg(BindSignalReceiverPayload.class, BindSignalReceiverPayload.STREAM_CODEC, BindSignalReceiverPayload::handleOnServer);
        reg(SetSignalAspectPayload.class, SetSignalAspectPayload.STREAM_CODEC, SetSignalAspectPayload::handleOnServer);
        reg(SetSignalValuePayload.class, SetSignalValuePayload.STREAM_CODEC, SetSignalValuePayload::handleOnServer);
        reg(ConfigureTrainDetectorPayload.class, ConfigureTrainDetectorPayload.STREAM_CODEC, ConfigureTrainDetectorPayload::handleOnServer);
        reg(ConfigureMarkerPayload.class, ConfigureMarkerPayload.STREAM_CODEC, ConfigureMarkerPayload::handleOnServer);
        reg(MarkerAnchorPayload.class, MarkerAnchorPayload.STREAM_CODEC, MarkerAnchorPayload::handleOnServer);
        reg(UpdateScriptBlockPayload.class, UpdateScriptBlockPayload.STREAM_CODEC, UpdateScriptBlockPayload::handleOnServer);
        reg(TrainScriptDataPayload.class, TrainScriptDataPayload.STREAM_CODEC, TrainScriptDataPayload::handleOnClient);
        reg(CarScriptDataPayload.class, CarScriptDataPayload.STREAM_CODEC, CarScriptDataPayload::handleOnServer);
        reg(CarScriptDataSyncPayload.class, CarScriptDataSyncPayload.STREAM_CODEC, CarScriptDataSyncPayload::handleOnClient);
        reg(SpeakerPlayPayload.class, SpeakerPlayPayload.STREAM_CODEC, SpeakerPlayPayload::handleOnClient);
        reg(SpeakerStopPayload.class, SpeakerStopPayload.STREAM_CODEC, SpeakerStopPayload::handleOnClient);
        reg(ConfigureSpeakerPayload.class, ConfigureSpeakerPayload.STREAM_CODEC, ConfigureSpeakerPayload::handleOnServer);
        reg(SyncSpeakerSoundsPayload.class, SyncSpeakerSoundsPayload.STREAM_CODEC, SyncSpeakerSoundsPayload::handleOnClient);
        reg(SignalControllerPayload.class, SignalControllerPayload.STREAM_CODEC, SignalControllerPayload::handleOnServer);
        reg(ChangeOffsetPayload.class, ChangeOffsetPayload.STREAM_CODEC, ChangeOffsetPayload::handleOnServer);
        reg(SaveSignboardPayload.class, SaveSignboardPayload.STREAM_CODEC, SaveSignboardPayload::handleOnServer);
        reg(ConfigureDetectorPayload.class, ConfigureDetectorPayload.STREAM_CODEC, ConfigureDetectorPayload::handleOnServer);
        reg(BuyTicketPayload.class, BuyTicketPayload.STREAM_CODEC, BuyTicketPayload::handleOnServer);
        reg(SetObjectModelPayload.class, SetObjectModelPayload.STREAM_CODEC, SetObjectModelPayload::handleOnServer);
        reg(MotormanMacroPayload.class, MotormanMacroPayload.STREAM_CODEC, MotormanMacroPayload::handleOnServer);
        reg(MotormanSkinPayload.class, MotormanSkinPayload.STREAM_CODEC, MotormanSkinPayload::handleOnServer);
        reg(DataMapSyncPayload.class, DataMapSyncPayload.STREAM_CODEC, DataMapSyncPayload::handleOnClient);
        reg(RtmuSettingsPayload.class, RtmuSettingsPayload.STREAM_CODEC, RtmuSettingsPayload::handleOnServer);
    }

    private static <MSG extends CustomPacketPayload> void reg(
            Class<MSG> type,
            StreamCodec<? super FriendlyByteBuf, MSG> codec,
            BiConsumer<MSG, IPayloadContext> handler) {
        CHANNEL.registerMessage(nextId++, type,
                (message, buffer) -> codec.encode(buffer, message),
                codec::decode,
                (message, contextSupplier) -> {
                    NetworkEvent.Context context = contextSupplier.get();
                    handler.accept(message, new ForgePayloadContext(context));
                    context.setPacketHandled(true);
                });
    }

    private record ForgePayloadContext(NetworkEvent.Context context) implements IPayloadContext {
        @Override
        public Player player() {
            return context.getSender();
        }

        @Override
        public CompletableFuture<Void> enqueueWork(Runnable work) {
            return context.enqueueWork(work);
        }
    }
}
