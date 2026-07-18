package com.myname.legacyloader.bridge.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * 1.7.10莠呈鋤縺ｮItemStack繝輔ぃ繧ｯ繝医Μ
 */
public class LegacyItemStack {

    /**
     * 繝｡繧ｿ繝・・繧ｿ莉倥″ItemStack繧剃ｽ懈・
     * 1.20.1縺ｧ縺ｯ繝繝｡繝ｼ繧ｸ蛟､縺ｨ縺励※繝｡繧ｿ繝・・繧ｿ繧剃ｿ晏ｭ・
     */
    public static ItemStack create(Item item, int count, int metadata) {
        ItemStack stack = new ItemStack(item, count);
        if (metadata != 0) {
            LegacyItemStackHelper.setMetadata(stack, metadata);
        }
        return stack;
    }

    public static ItemStack create(Item item, int count) {
        return create(item, count, 0);
    }

    public static ItemStack create(Item item) {
        return create(item, 1, 0);
    }

    public static ItemStack create(Block block, int count, int metadata) {
        Item item = Item.byBlock(block);
        return create(item, count, metadata);
    }

    public static ItemStack create(Block block, int count) {
        return create(block, count, 0);
    }

    public static ItemStack create(Block block) {
        return create(block, 1, 0);
    }

    /**
     * 繝｡繧ｿ繝・・繧ｿ繧貞叙蠕暦ｼ・.7.10莠呈鋤・・
     */
    public static int getMetadata(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return LegacyItemStackHelper.getMetadata(stack);
    }

    /**
     * 繝｡繧ｿ繝・・繧ｿ繧定ｨｭ螳・
     */
    public static void setMetadata(ItemStack stack, int metadata) {
        if (stack != null && !stack.isEmpty()) {
            LegacyItemStackHelper.setMetadata(stack, metadata);
        }
    }
}
