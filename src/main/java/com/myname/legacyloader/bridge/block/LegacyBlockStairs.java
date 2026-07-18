package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.client.LegacyIcon;
import com.myname.legacyloader.bridge.client.LegacyIconRegister;
import com.myname.legacyloader.bridge.item.LegacyCreativeTab;
import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LegacyBlockStairs extends StairBlock implements ILegacyBlock {
    public String legacyTextureName;
    public String legacyUnlocalizedName;
    public LegacyIcon field_149761_L;
    public final LegacyMaterial legacyMaterial;
    private LegacyCreativeTab assignedTab;

    // 笘・ｿｽ蜉: 蜈・ヶ繝ｭ繝・け縺ｮ繝｡繧ｿ繝・・繧ｿ繧剃ｿ晏ｭ・
    private final int sourceMetadata;
    private final Block sourceBlock;

    public LegacyBlockStairs(Block modelBlock, int meta) {
        super(modelBlock != null ? modelBlock.defaultBlockState() : Blocks.STONE.defaultBlockState(),
                BlockBehaviour.Properties.of().strength(2.0f, 10.0f));

        this.sourceBlock = modelBlock;
        this.sourceMetadata = meta;

        LegacyMaterial mat = LegacyMaterial.ROCK;
        if (modelBlock instanceof ILegacyBlock) {
            mat = ((ILegacyBlock) modelBlock).getMaterial();
            // 笘・ユ繧ｯ繧ｹ繝√Ε蜷阪ｒ蜈・ヶ繝ｭ繝・け縺九ｉ邯呎価
            String srcTex = ((ILegacyBlock) modelBlock).getTextureName();
            if (srcTex != null) {
                this.legacyTextureName = srcTex;
            }
        }
        this.legacyMaterial = mat != null ? mat : LegacyMaterial.ROCK;

        LegacyGameRegistry.trackBlock(this);
    }

    // 笘・ｿｽ蜉: 繧ｽ繝ｼ繧ｹ繝｡繧ｿ繝・・繧ｿ繧貞叙蠕・
    public int getSourceMetadata() {
        return sourceMetadata;
    }

    public Block getSourceBlock() {
        return sourceBlock;
    }

    @Override
    public LegacyMaterial getMaterial() {
        return this.legacyMaterial;
    }

    @Override
    public String getTextureName() {
        return this.legacyTextureName;
    }

    @Override
    public Block setBlockTextureName(String name) {
        this.legacyTextureName = name;
        return this;
    }

    @Override
    public Block setBlockName(String name) {
        this.legacyUnlocalizedName = name;
        return this;
    }

    @Override
    public Block setCreativeTab(LegacyCreativeTab tab) {
        this.assignedTab = tab;
        if (tab != null) tab.addItem(this);
        return this;
    }

    public LegacyCreativeTab getCreativeTab() {
        return this.assignedTab;
    }

    @Override
    public Block setHardness(float hardness) { return this; }
    @Override
    public Block setResistance(float resistance) { return this; }
    @Override
    public Block setLightLevel(float value) { return this; }
    @Override
    public Block setLightOpacity(int opacity) { return this; }
    @Override
    public Block setStepSound(LegacySoundType sound) { return this; }
    public Block setStepSound(Object sound) { return this; }
    @Override
    public void setHarvestLevel(String toolClass, int level) {}
    @Override
    public void setHarvestLevel(String toolClass, int level, int metadata) {}
    @Override
    public void registerBlockIcons(LegacyIconRegister reg) {}

    public void func_149651_a(LegacyIconRegister register) { registerBlockIcons(register); }
    public void func_149651_a(Object register) {
        if (register instanceof LegacyIconRegister) registerBlockIcons((LegacyIconRegister) register);
    }

    public LegacyIcon func_149691_a(int side, int meta) { return getIcon(side, meta); }
    public LegacyIcon getIcon(int side, int meta) { return field_149761_L; }
    public LegacyIcon func_149673_e(net.minecraft.world.level.BlockGetter world, int x, int y, int z, int side) {
        return getIcon(side, 0);
    }
    public int func_149645_b() { return 0; }
    public boolean func_149662_c() { return false; }
    public boolean func_149686_d() { return false; }
    public boolean func_149646_a(net.minecraft.world.level.BlockGetter world, int x, int y, int z, int side) { return true; }

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
    public Block func_149647_a(Object tab) {
        if (tab instanceof LegacyCreativeTab) return setCreativeTab((LegacyCreativeTab) tab);
        return this;
    }
    public Block func_149711_c(float hardness) { return setHardness(hardness); }
    public Block func_149752_b(float resistance) { return setResistance(resistance); }
    public Block func_149672_a(LegacySoundType sound) { return setStepSound(sound); }
    public Block func_149672_a(Object sound) { return setStepSound(sound); }
    public Block func_149713_g(int opacity) { return setLightOpacity(opacity); }
    public Block func_149715_a(float value) { return setLightLevel(value); }
    public LegacyMaterial func_149688_o() { return getMaterial(); }
    public String func_149641_N() { return getTextureName(); }
}
