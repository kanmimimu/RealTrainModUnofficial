package com.portofino.realtrainmodunofficial;

import com.portofino.realtrainmodunofficial.client.PackRequirementWarnings;
import com.portofino.realtrainmodunofficial.modelpack.VehicleModelPackManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// このクラスは専用サーバーではロードされません。
// TODO(Phase3): NeoForge の 第2@Mod(dist=CLIENT) + ConfigurationScreen 構造を Forge 化する。
//   (1) config 画面は NeoForge 自動生成のため Forge 1.20.1 に相当が無く保留 (constructor 削除済)。
//   (2) onClientSetup は MOD バス, onLoggingOut は FORGE バスなので、本来 @EventBusSubscriber を
//       バスごとに分割する必要がある。ここでは setup(script/pack 初期化)を優先し MOD バスに固定。
@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class RealTrainModUnofficialClient {

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // クライアントのセットアップ・コード
        RealTrainModUnofficial.LOGGER.info("HELLO FROM CLIENT SETUP");
        RealTrainModUnofficial.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        com.portofino.realtrainmodunofficial.script.TrainScriptSystem.getInstance().initialize();
        VehicleModelPackManager.INSTANCE.initialize(Minecraft.getInstance().getResourceManager());
        PackRequirementWarnings.refresh();
        //オンライン連携 (GitHub アップデート確認 + 公式サイトの BAN リスト)。バックグラウンドで実行。
        com.portofino.realtrainmodunofficial.online.RtmuOnlineServices.init();
    }

    /**
     * ワールドを抜けたら看板まわりのキャッシュを捨てる。
     * <p>
     * 看板の文字は OS フォントを焼いた GL テクスチャなので、放っておくとワールドを
     * 出入りするたびに溜まっていく。時刻表も破棄して、ユーザーが
     * config/realtrainmodunofficial/timetable/ に置いた tt_*.csv を再入場で拾えるようにする。
     */
    @SubscribeEvent
    static void onLoggingOut(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        com.portofino.realtrainmodunofficial.client.signboard.FontImage.clearCache();
        com.portofino.realtrainmodunofficial.client.signboard.tt.TimeTableManager.INSTANCE.invalidate();
    }
}
