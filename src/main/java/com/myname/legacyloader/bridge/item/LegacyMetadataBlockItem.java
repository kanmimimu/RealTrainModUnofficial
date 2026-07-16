package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.block.ILegacyBlock;
import com.myname.legacyloader.bridge.block.LegacyBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

public class LegacyMetadataBlockItem extends LegacyBlockItem {

    public static final List<LegacyMetadataBlockItem> ALL_META_INSTANCES = new ArrayList<>();

    private final int maxMetadata;

    public LegacyMetadataBlockItem(Block block) {
        this(block, 16);
    }

    public LegacyMetadataBlockItem(Block block, int maxMetadata) {
        super(block, new Item.Properties());
        this.maxMetadata = maxMetadata;
        this.setHasSubtypes(true);
        ALL_META_INSTANCES.add(this);
    }

    public LegacyMetadataBlockItem(Block block, Block unused) {
        this(block);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) return null;

        // 笘・ｿｮ豁｣: LegacyItemStackHelper繧剃ｽｿ逕ｨ
        int meta = LegacyItemStackHelper.getMetadata(context.getItemInHand());

        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equals("metadata") && prop instanceof IntegerProperty) {
                IntegerProperty intProp = (IntegerProperty) prop;
                if (intProp.getPossibleValues().contains(meta)) {
                    state = state.setValue(intProp, meta);
                }
                break;
            }
        }

        return state;
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

    public List<ItemStack> getSubItems() {
        List<ItemStack> list = new ArrayList<>();

        Block block = getBlock();
        if (block instanceof ILegacyBlock) {
            ILegacyBlock legacyBlock = (ILegacyBlock) block;
            LegacyCreativeTab tab = null;
            if (block instanceof LegacyBlock) {
                tab = ((LegacyBlock) block).getCreativeTab();
            }
            legacyBlock.getSubBlocks(this, tab, list);
        }

        if (list.isEmpty()) {
            for (int i = 0; i < maxMetadata; i++) {
                ItemStack stack = new ItemStack(this);
                LegacyItemStackHelper.setMetadata(stack, i);
                list.add(stack);
            }
        }

        return list;
    }
}