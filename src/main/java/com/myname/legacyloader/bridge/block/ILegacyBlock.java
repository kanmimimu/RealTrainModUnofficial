package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.client.LegacyIconRegister;
import com.myname.legacyloader.bridge.item.LegacyCreativeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface ILegacyBlock {
    String getTextureName();
    default String getLegacyTextureName() { return getTextureName(); }

    LegacyMaterial getMaterial();

    Block setBlockName(String name);
    Block setBlockTextureName(String name);
    Block setCreativeTab(LegacyCreativeTab tab);
    Block setHardness(float hardness);
    Block setResistance(float resistance);
    Block setLightLevel(float value);
    Block setLightOpacity(int opacity);
    Block setStepSound(LegacySoundType sound);

    void setHarvestLevel(String toolClass, int level);
    void setHarvestLevel(String toolClass, int level, int metadata);

    void registerBlockIcons(LegacyIconRegister reg);
    default void func_149651_a(LegacyIconRegister reg) { registerBlockIcons(reg); }

    // 笘・Γ繧ｿ繝・・繧ｿ髢｢騾｣
    default void getSubBlocks(Item item, LegacyCreativeTab tab, List<ItemStack> list) {
        list.add(new ItemStack(item));
    }

    // 笘・RG蜷阪お繧､繝ｪ繧｢繧ｹ - 隍・焚縺ｮ繧ｷ繧ｰ繝阪メ繝｣縺ｫ蟇ｾ蠢・
    default void func_149666_a(Item item, LegacyCreativeTab tab, List<ItemStack> list) {
        getSubBlocks(item, tab, list);
    }

    default void func_149666_a(Item item, Object tab, List<ItemStack> list) {
        if (tab instanceof LegacyCreativeTab) {
            getSubBlocks(item, (LegacyCreativeTab) tab, list);
        } else {
            getSubBlocks(item, null, list);
        }
    }

    default int damageDropped(int meta) {
        return 0;
    }

    default int func_149692_a(int meta) {
        return damageDropped(meta);
    }

    default boolean hasSubtypes() {
        return false;
    }

    /**
     * 繝｡繧ｿ繝・・繧ｿ縺ｮ譛螟ｧ謨ｰ繧貞叙蠕・
     */
    default int getMaxMetadata() {
        return 1;
    }
}