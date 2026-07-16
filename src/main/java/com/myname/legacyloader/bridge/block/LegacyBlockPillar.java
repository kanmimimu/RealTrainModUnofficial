package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.client.LegacyIconRegister;
import com.myname.legacyloader.bridge.item.LegacyCreativeTab;
import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LegacyBlockPillar extends RotatedPillarBlock implements ILegacyBlock {
    public String legacyTextureName;
    public String legacyUnlocalizedName;
    public final LegacyMaterial legacyMaterial;
    private LegacyCreativeTab assignedTab;

    public LegacyBlockPillar() {
        this(LegacyMaterial.WOOD);
    }

    public LegacyBlockPillar(LegacyMaterial material) {
        super(BlockBehaviour.Properties.of()
                .mapColor(material != null ? material.getColor() : LegacyMaterial.WOOD.getColor())
                .strength(2.0F));
        this.legacyMaterial = material != null ? material : LegacyMaterial.WOOD;
        LegacyGameRegistry.trackBlock(this);
    }

    // === ILegacyBlock Implementation ===

    @Override
    public LegacyMaterial getMaterial() {
        return this.legacyMaterial;
    }

    @Override
    public String getTextureName() {
        return legacyTextureName;
    }

    @Override
    public Block setBlockName(String name) {
        this.legacyUnlocalizedName = name;
        return this;
    }

    @Override
    public Block setBlockTextureName(String name) {
        this.legacyTextureName = name;
        return this;
    }

    @Override
    public Block setCreativeTab(LegacyCreativeTab tab) {
        this.assignedTab = tab;
        if (tab != null) tab.addItem(this);
        return this;
    }

    @Override
    public Block setHardness(float h) {
        return this;
    }

    @Override
    public Block setResistance(float r) {
        return this;
    }

    @Override
    public Block setLightLevel(float v) {
        return this;
    }

    @Override
    public Block setLightOpacity(int o) {
        return this;
    }

    @Override
    public Block setStepSound(LegacySoundType s) {
        return this;
    }

    public Block setStepSound(Object s) {
        return this;
    }

    @Override
    public void setHarvestLevel(String t, int l) {
    }

    @Override
    public void setHarvestLevel(String t, int l, int m) {
    }

    @Override
    public void registerBlockIcons(LegacyIconRegister r) {
    }

    @Override
    public String getDescriptionId() {
        if (this.legacyUnlocalizedName != null) {
            return "tile." + this.legacyUnlocalizedName;
        }
        return super.getDescriptionId();
    }

    // === SRG Method Aliases ===

    public Block func_149663_c(String name) { return setBlockName(name); }
    public Block func_149658_d(String name) { return setBlockTextureName(name); }
    public Block func_149647_a(LegacyCreativeTab tab) { return setCreativeTab(tab); }
    public Block func_149711_c(float hardness) { return setHardness(hardness); }
    public Block func_149752_b(float resistance) { return setResistance(resistance); }
    public Block func_149672_a(LegacySoundType sound) { return setStepSound(sound); }
    public Block func_149672_a(Object sound) { return setStepSound(sound); }
    public Block func_149713_g(int opacity) { return setLightOpacity(opacity); }
    public Block func_149715_a(float value) { return setLightLevel(value); }
    public LegacyMaterial func_149688_o() { return getMaterial(); }
    public String func_149641_N() { return getTextureName(); }
}