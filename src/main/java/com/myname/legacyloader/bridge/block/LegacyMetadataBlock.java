package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.item.LegacyCreativeTab;
import com.myname.legacyloader.bridge.item.LegacyItemStackHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

/**
 * 繝｡繧ｿ繝・・繧ｿ蟇ｾ蠢懊ヶ繝ｭ繝・け
 * 1.7.10縺ｮ繝｡繧ｿ繝・・繧ｿ(0-15)繧達lockState繝励Ο繝代ユ繧｣縺ｨ縺励※繧ｨ繝溘Η繝ｬ繝ｼ繝・
 */
public class LegacyMetadataBlock extends LegacyBlock {

    // 繝｡繧ｿ繝・・繧ｿ繝励Ο繝代ユ繧｣・・-15・・
    public static final IntegerProperty METADATA = LegacyBlock.METADATA;

    protected final int maxMetadata;

    public LegacyMetadataBlock(LegacyMaterial material) {
        this(material, 16);
    }

    public LegacyMetadataBlock(LegacyMaterial material, int variants) {
        super(material);
        this.maxMetadata = Math.min(variants, 16);
        // 繝・ヵ繧ｩ繝ｫ繝医せ繝・・繝医ｒ險ｭ螳・
    }

    /**
     * 繝｡繧ｿ繝・・繧ｿ縺九ｉBlockState繧貞叙蠕・
     */
    public BlockState getStateFromMeta(int meta) {
        return this.defaultBlockState().setValue(METADATA, Math.min(meta, maxMetadata - 1));
    }

    /**
     * BlockState縺九ｉ繝｡繧ｿ繝・・繧ｿ繧貞叙蠕・
     */
    public int getMetaFromState(BlockState state) {
        return state.getValue(METADATA);
    }

    @Override
    public void getSubBlocks(Item item, LegacyCreativeTab tab, List<ItemStack> list) {
        for (int i = 0; i < maxMetadata; i++) {
            ItemStack stack = new ItemStack(item);
            LegacyItemStackHelper.setMetadata(stack, i);
            list.add(stack);
        }
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public boolean hasSubtypes() {
        return maxMetadata > 1;
    }
}
