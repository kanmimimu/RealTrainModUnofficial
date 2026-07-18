package com.portofino.realtrainmodunofficial.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * 列車の半透明バッチ (窓ガラス等) 専用の遅延描画バッファ。
 *
 * <p>問題: 車両ごとに「不透明 → ガラス」を描くと、エンティティ用の非固定 RenderType は
 * 次の RenderType 要求時に途中フラッシュされるため、A車のガラスが B車の座席より先に
 * 深度付きで描かれることがある。エンティティの描画順は毎フレーム変わるので、
 * 「別の車両 (や同編成の隣の車) のガラス越しに座席が見えたり消えたりする」チラつきになる。
 *
 * <p>対策: 列車のガラスはこの専用バッファに溜め、地形の半透明 (ガラスブロック等) まで
 * 全て描き終わった AFTER_TRANSLUCENT_BLOCKS ステージで一括描画する。
 * これで座席 (不透明) は常にガラスより先に描かれ、どの角度・順序でも透けて見える。
 * シェーダーパック使用時は RenderType の挙動が変わるため従来経路のまま。
 */
@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, value = Dist.CLIENT)
public final class DeferredTranslucentRenderer {

    private static final MultiBufferSource.BufferSource BUFFER =
            MultiBufferSource.immediate(new BufferBuilder(1536));

    private DeferredTranslucentRenderer() {
    }

    /** この entity の半透明バッチを遅延バッファへ回すか。 */
    public static boolean shouldDefer(Object entity) {
        if (ShaderCompat.isShaderPackInUse()) {
            return false;
        }
        return entity instanceof jp.ngt.rtm.entity.vehicle.EntityVehicleBase
                || entity instanceof com.portofino.realtrainmodunofficial.entity.TrainEntity
                || entity instanceof com.portofino.realtrainmodunofficial.entity.CarEntity;
    }

    public static MultiBufferSource buffer() {
        return BUFFER;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        //地形の半透明の後 = 画面内の不透明物すべての後。ここでガラスを一括描画。
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            BUFFER.endBatch();
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            //安全網: 何らかの理由で上のステージが飛んでも残さない
            BUFFER.endBatch();
        }
    }
}
