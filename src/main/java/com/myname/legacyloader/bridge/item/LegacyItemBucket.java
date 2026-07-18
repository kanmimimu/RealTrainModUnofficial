package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;

public class LegacyItemBucket extends BucketItem {

    // 1.7.10縺ｮ繝輔ぅ繝ｼ繝ｫ繝・
    public Block isFull;
    public Block field_77876_a;

    public LegacyItemBucket(Block containedBlock) {
        // 繝舌ル繝ｩ縺ｮBucketItem繧ｳ繝ｳ繧ｹ繝医Λ繧ｯ繧ｿ縺ｫ蜷医ｏ縺帙ｋ
        super(Fluids.WATER, new Item.Properties().stacksTo(1));

        this.isFull = containedBlock;
        this.field_77876_a = containedBlock;
        LegacyGameRegistry.trackItem(this);
    }

    // Mod縺後が繝ｼ繝舌・繝ｩ繧､繝峨☆繧句庄閭ｽ諤ｧ縺ｮ縺ゅｋ繝｡繧ｽ繝・ラ縺ｮ繝繝溘・
    public boolean tryPlaceContainedLiquid(Object world, int x, int y, int z) {
        return false;
    }
    public boolean func_77875_a(Object world, int x, int y, int z) {
        return tryPlaceContainedLiquid(world, x, y, z);
    }

    public Item func_77642_a(Item container) {
        return this;
    }
}
