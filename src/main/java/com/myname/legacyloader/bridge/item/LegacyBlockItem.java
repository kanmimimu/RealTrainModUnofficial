package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.block.LegacyMetadataBlock;
import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

public class LegacyBlockItem extends BlockItem {

    public static final List<LegacyBlockItem> ALL_INSTANCES = new ArrayList<>();

    protected boolean hasSubtypes = false;

    public LegacyBlockItem(Block block) {
        super(block, new Item.Properties());
        LegacyGameRegistry.trackItem(this);
        ALL_INSTANCES.add(this);
    }

    public LegacyBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
        LegacyGameRegistry.trackItem(this);
        ALL_INSTANCES.add(this);
    }

    public void setHasSubtypes(boolean hasSubtypes) {
        this.hasSubtypes = hasSubtypes;
    }

    public boolean getHasSubtypes() {
        return this.hasSubtypes;
    }

    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) return null;

        Block block = getBlock();

        // 繝｡繧ｿ繝・・繧ｿ繝悶Ο繝・け縺ｮ蝣ｴ蜷医・縺ｿ繝｡繧ｿ繝・・繧ｿ繧帝←逕ｨ
        if (block instanceof LegacyMetadataBlock) {
            int meta = LegacyItemStackHelper.getMetadata(context.getItemInHand());
            if (state.hasProperty(LegacyMetadataBlock.METADATA)) {
                // 繝｡繧ｿ繝・・繧ｿ縺梧怏蜉ｹ遽・峇蜀・°繝√ぉ繝・け
                if (meta >= 0 && meta <= 15) {
                    state = state.setValue(LegacyMetadataBlock.METADATA, meta);
                }
            }
        }

        int legacyMeta = LegacyItemStackHelper.getMetadata(context.getItemInHand());
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof IntegerProperty intProp && "metadata".equals(prop.getName())
                    && intProp.getPossibleValues().contains(legacyMeta)) {
                state = state.setValue(intProp, legacyMeta);
                break;
            }
        }

        return state;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        Block block = getBlock();

        if (block instanceof LegacyBlock) {
            LegacyBlock lb = (LegacyBlock) block;
            if (lb.legacyUnlocalizedName != null) {
                return "tile." + lb.legacyUnlocalizedName;
            }
        }

        return super.getDescriptionId(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack));
    }

    // === SRG蜷阪お繧､繝ｪ繧｢繧ｹ ===

    public Item func_77655_b(String name) {
        return this;
    }

    public LegacyBlockItem setUnlocalizedName(String name) {
        func_77655_b(name);
        return this;
    }

    public int func_77647_a(int damage) {
        return getMetadata(damage);
    }

    public Item func_77625_d(int size) {
        return this;
    }

    public LegacyBlockItem setMaxStackSize(int size) {
        func_77625_d(size);
        return this;
    }

    public Item func_77656_e(int damage) {
        return this;
    }

    public LegacyBlockItem setMaxDamage(int damage) {
        func_77656_e(damage);
        return this;
    }

    public Item func_77627_a(boolean hasSubtypes) {
        setHasSubtypes(hasSubtypes);
        return this;
    }

    public LegacyBlockItem setHasSubtypesCompat(boolean hasSubtypes) {
        func_77627_a(hasSubtypes);
        return this;
    }

    public Item func_77668_e() {
        return this;
    }

    public LegacyBlockItem setFull3D() {
        func_77668_e();
        return this;
    }

    public Item func_77612_l() {
        return this;
    }

    public LegacyBlockItem setNoRepair() {
        func_77612_l();
        return this;
    }

    public Item func_77642_a(Item containerItem) {
        return this;
    }

    public LegacyBlockItem setContainerItem(Item containerItem) {
        func_77642_a(containerItem);
        return this;
    }

    public Item func_111206_d(String textureName) {
        return this;
    }

    public LegacyBlockItem setTextureName(String textureName) {
        func_111206_d(textureName);
        return this;
    }

    public Item func_77637_a(LegacyCreativeTab tab) {
        return this;
    }

    public LegacyBlockItem setCreativeTab(LegacyCreativeTab tab) {
        func_77637_a(tab);
        return this;
    }

    public String func_77658_a() {
        return getDescriptionId();
    }
}
