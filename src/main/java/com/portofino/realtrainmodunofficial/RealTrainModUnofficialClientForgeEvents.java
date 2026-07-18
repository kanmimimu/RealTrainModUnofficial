package com.portofino.realtrainmodunofficial;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public final class RealTrainModUnofficialClientForgeEvents {
    private RealTrainModUnofficialClientForgeEvents() {
    }

    /**
     * ワールドを抜けたら看板まわりのキャッシュを捨てる。
     * <p>
     * 看板の文字は OS フォントを焼いた GL テクスチャなので、放っておくとワールドを
     * 出入りするたびに溜まっていく。時刻表も破棄して、ユーザーが
     * config/realtrainmodunofficial/timetable/ に置いた tt_*.csv を再入場で拾えるようにする。
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        com.portofino.realtrainmodunofficial.client.signboard.FontImage.clearCache();
        com.portofino.realtrainmodunofficial.client.signboard.tt.TimeTableManager.INSTANCE.invalidate();
    }
}
