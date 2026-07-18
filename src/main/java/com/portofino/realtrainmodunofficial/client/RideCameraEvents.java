package com.portofino.realtrainmodunofficial.client;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.client.camera.RtmCamera;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * 列車に乗っているときだけ、三人称視点の距離をホイールで遠ざけられるようにする。
 * 編成全体を見渡せるように、バニラ固定の 4 ブロックより大きく引ける。
 *
 * <p>シネマカメラ ({@link RtmCamera}) が有効なときはそちらがホイールを持つので手を出さない。
 * 一人称のときも何もしない (通常のホットバー操作を残す)。
 */
@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, value = Dist.CLIENT)
public final class RideCameraEvents {

    private static final float MIN_DISTANCE = 4.0F;   //バニラと同じ最短
    private static final float MAX_DISTANCE = 48.0F;  //編成を丸ごと見渡せる程度
    private static final float STEP = 2.0F;           //ホイール 1 ノッチあたり
    private static float distance = MIN_DISTANCE;

    private RideCameraEvents() {
    }

    private static boolean ridingTrain(Minecraft mc) {
        return mc.player != null && mc.player.getVehicle() instanceof EntityTrainBase;
    }

    private static boolean isThirdPerson(Minecraft mc) {
        return !mc.options.getCameraType().isFirstPerson();
    }

    /** 乗車中 + 三人称のときだけ、ホイールでカメラ距離を調整する。 */
    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || RtmCamera.INSTANCE.isActive()) {
            return;
        }
        if (!ridingTrain(mc) || !isThirdPerson(mc)) {
            return;
        }
        double dy = event.getScrollDelta();
        if (dy != 0.0D) {
            //ホイール奥 (up, dy>0) = 引く (遠ざける)、手前 (down) = 寄る。
            distance = Mth.clamp(distance + (float) dy * STEP, MIN_DISTANCE, MAX_DISTANCE);
            event.setCanceled(true);
        }
    }

    // TODO(Phase3): 乗車中の三人称カメラ最大距離の上書き。NeoForge の
    // CalculateDetachedCameraDistanceEvent は Forge 1.20.1 に相当イベントが無いため保留。
    // (Forge では ViewportEvent.ComputeCameraAngles では距離を扱えず、Mixin 等で代替検討)
    // distance フィールドはマウスホイール入力(mouseScrolled)側で維持されている。
}
