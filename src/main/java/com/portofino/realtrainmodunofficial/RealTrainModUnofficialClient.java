package com.portofino.realtrainmodunofficial;

import com.portofino.realtrainmodunofficial.client.PackRequirementWarnings;
import com.portofino.realtrainmodunofficial.modelpack.VehicleModelPackManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// このクラスは専用サーバーではロードされません。
// MOD バス専用。FORGE バス用のクライアントイベントは
// RealTrainModUnofficialClientForgeEvents を参照。
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
        RtmuSettings.load();
        //オンライン連携 (GitHub アップデート確認 + 公式サイトの BAN リスト)。バックグラウンドで実行。
        com.portofino.realtrainmodunofficial.online.RtmuOnlineServices.init();
    }
}
