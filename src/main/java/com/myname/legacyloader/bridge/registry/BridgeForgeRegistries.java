package com.myname.legacyloader.bridge.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

// ForgeRegistries 縺ｮ蛛ｽ迚ｩ
public class BridgeForgeRegistries {
    // 譛ｬ迚ｩ縺ｮ繝ｬ繧ｸ繧ｹ繝医Μ繧・BridgeRegistry 縺ｧ繝ｩ繝・・縺励※蜈ｬ髢九☆繧・

    public static final BridgeRegistry<Block> BLOCKS = new BridgeRegistry<>(BuiltInRegistries.BLOCK);
    public static final BridgeRegistry<Item> ITEMS = new BridgeRegistry<>(BuiltInRegistries.ITEM);

    // 蠢・ｦ√↓蠢懊§縺ｦ莉悶・繝ｬ繧ｸ繧ｹ繝医Μ繧ゅ％縺薙↓霑ｽ蜉縺励※縺上□縺輔＞
    // 萓・
    // public static final BridgeRegistry<EntityType<?>> ENTITY_TYPES = new BridgeRegistry<>(ForgeRegistries.ENTITY_TYPES);
    // public static final BridgeRegistry<SoundEvent> SOUND_EVENTS = new BridgeRegistry<>(ForgeRegistries.SOUND_EVENTS);
}