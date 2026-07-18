package com.myname.legacyloader.bridge.fml;

import net.neoforged.bus.api.IEventBus;

public class BridgeModLoadingContext {
    private static final BridgeModLoadingContext INSTANCE = new BridgeModLoadingContext();
    private final BridgeEventBus bus = new BridgeEventBus();

    public static BridgeModLoadingContext get() {
        return INSTANCE;
    }

    // 笘・､画峩轤ｹ: 謌ｻ繧雁､繧・BridgeEventBus 縺ｧ縺ｯ縺ｪ縺・IEventBus 縺ｫ縺吶ｋ
    public IEventBus getModEventBus() {
        return bus;
    }

    // LegacyLoaderMod縺九ｉ蜻ｼ縺ｳ蜃ｺ縺吶◆繧√・繝｡繧ｽ繝・ラ・亥梛螟画鋤逕ｨ・・
    public BridgeEventBus getBridgeBus() {
        return bus;
    }
}