package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.block.LegacyBlockSlab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class LegacySlabBlockItem extends LegacyBlockItem {

    public static final List<LegacySlabBlockItem> ALL_INSTANCES = new ArrayList<>();

    private final LegacyBlockSlab singleSlab;
    private final LegacyBlockSlab doubleSlab;

    public LegacySlabBlockItem(LegacyBlockSlab singleSlab, LegacyBlockSlab doubleSlab) {
        super(singleSlab, new Item.Properties());
        this.singleSlab = singleSlab;
        this.doubleSlab = doubleSlab;
        this.setHasSubtypes(hasMetadataVariants(singleSlab));
        ALL_INSTANCES.add(this);
    }

    public LegacySlabBlockItem(Block block) {
        super(block, new Item.Properties());
        if (block instanceof LegacyBlockSlab slab) {
            if (slab.isDouble) {
                this.doubleSlab = slab;
                this.singleSlab = slab.getPairedSlab();
            } else {
                this.singleSlab = slab;
                this.doubleSlab = slab.getPairedSlab();
            }
        } else {
            this.singleSlab = null;
            this.doubleSlab = null;
        }
        this.setHasSubtypes(hasMetadataVariants(this.singleSlab));
        ALL_INSTANCES.add(this);
    }

    private static boolean hasMetadataVariants(LegacyBlockSlab slab) {
        return slab != null && slab.getMaxVariants() > 1;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (singleSlab != null && singleSlab.legacyUnlocalizedName != null) {
            return "tile." + singleSlab.legacyUnlocalizedName;
        }
        return super.getDescriptionId(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack));
    }

    public LegacyBlockSlab getSingleSlab() {
        return singleSlab;
    }

    public LegacyBlockSlab getDoubleSlab() {
        return doubleSlab;
    }
}
