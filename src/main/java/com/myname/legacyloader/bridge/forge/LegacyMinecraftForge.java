package com.myname.legacyloader.bridge.forge;

import com.myname.legacyloader.bridge.fml.BridgeEventBus;

public class LegacyMinecraftForge {
    // 1.7.10: public static final EventBus EVENT_BUS
    // BridgeEventBus 蝙九→縺励※螳夂ｾｩ縺励｀od縺後％繧後↓繧｢繧ｯ繧ｻ繧ｹ縺ｧ縺阪ｋ繧医≧縺ｫ縺励∪縺・
    public static final BridgeEventBus EVENT_BUS = new BridgeEventBus();
    public static final BridgeEventBus TERRAIN_GEN_BUS = new BridgeEventBus();
}
