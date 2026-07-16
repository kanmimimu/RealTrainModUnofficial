package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class LegacyItemBlockWithMetadata extends LegacyBlockItem {

    public static final List<LegacyItemBlockWithMetadata> ALL_META_INSTANCES = new ArrayList<>();

    public LegacyItemBlockWithMetadata(Block block) {
        super(block, new Item.Properties());
        this.setHasSubtypes(true);
        ALL_META_INSTANCES.add(this);
    }

    public LegacyItemBlockWithMetadata(Block block, Block unused) {
        this(block);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        // 笘・ｿｮ豁｣: LegacyItemStackHelper繧剃ｽｿ逕ｨ
        int meta = LegacyItemStackHelper.getMetadata(stack);
        Block block = getBlock();

        if (block instanceof LegacyBlock) {
            LegacyBlock lb = (LegacyBlock) block;
            if (lb.legacyUnlocalizedName != null) {
                return "tile." + lb.legacyUnlocalizedName + "_" + String.format("%02d", meta);
            }
        }

        return super.getDescriptionId(stack) + "_" + String.format("%02d", meta);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack));
    }
}