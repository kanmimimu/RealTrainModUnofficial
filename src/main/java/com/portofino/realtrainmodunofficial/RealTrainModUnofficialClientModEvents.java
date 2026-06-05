package com.portofino.realtrainmodunofficial;

import com.portofino.realtrainmodunofficial.client.renderer.RailCoreBlockEntityRenderer;
import com.portofino.realtrainmodunofficial.client.renderer.TrainBogieEntityRenderer;
import com.portofino.realtrainmodunofficial.client.renderer.TrainEntityRenderer;
import com.portofino.realtrainmodunofficial.client.renderer.TrainSeatEntityRenderer;
import com.portofino.realtrainmodunofficial.client.TrainControlKeyMappings;
import com.portofino.realtrainmodunofficial.client.renderer.CarRenderer;
import com.portofino.realtrainmodunofficial.client.renderer.InstalledObjectBlockEntityRenderer;
import com.portofino.realtrainmodunofficial.client.sound.ExternalSoundPackBridge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RealTrainModUnofficialClientModEvents {
    private RealTrainModUnofficialClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // レールコアのブロックエンティティレンダラーを登録（MQOモデル描画）
        event.registerBlockEntityRenderer(
            RealTrainModUnofficialBlockEntities.LARGE_RAIL_CORE.get(),
            RailCoreBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
            RealTrainModUnofficialBlockEntities.INSTALLED_OBJECT.get(),
            InstalledObjectBlockEntityRenderer::new
        );
        if (RealTrainModUnofficialEntities.TRAIN.isBound()) {
            event.registerEntityRenderer(
                RealTrainModUnofficialEntities.TRAIN.get(),
                TrainEntityRenderer::new
            );
        }
        if (RealTrainModUnofficialEntities.TRAIN_BOGIE.isBound()) {
            event.registerEntityRenderer(
                RealTrainModUnofficialEntities.TRAIN_BOGIE.get(),
                TrainBogieEntityRenderer::new
            );
        }
        if (RealTrainModUnofficialEntities.TRAIN_SEAT.isBound()) {
            event.registerEntityRenderer(
                RealTrainModUnofficialEntities.TRAIN_SEAT.get(),
                TrainSeatEntityRenderer::new
            );
        }
        event.registerEntityRenderer(
            com.portofino.realtrainmodunofficial.registry.RealTrainModUnofficialEntities.CAR.get(),
            CarRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        TrainControlKeyMappings.register(event);
    }

    @SubscribeEvent
    public static void registerPackFinders(AddPackFindersEvent event) {
        ExternalSoundPackBridge.register(event);
    }

    // 本家RTM同様、テクスチャ(白の marker_0 等)は変えず tint 色だけ変える。
    // 普通マーカー=赤、分岐マーカー=青。
    private static final int MARKER_COLOR = 0xFF3B30;        // 赤
    private static final int MARKER_SWITCH_COLOR = 0x0028C8; // 濃い青(本家寄り)

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
            (state, tintGetter, pos, tintIndex) -> MARKER_COLOR,
            RealTrainModUnofficialBlocks.MARKER.get()
        );
        event.register(
            (state, tintGetter, pos, tintIndex) -> MARKER_SWITCH_COLOR,
            RealTrainModUnofficialBlocks.MARKER_SWITCH.get()
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
            (stack, tintIndex) -> MARKER_COLOR,
            RealTrainModUnofficialItems.MARKER_ITEM.get()
        );
        event.register(
            (stack, tintIndex) -> MARKER_SWITCH_COLOR,
            RealTrainModUnofficialItems.MARKER_SWITCH_ITEM.get()
        );
    }

}
