package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.client.LegacyIcon;
import com.myname.legacyloader.bridge.client.LegacyIconRegister;
import com.myname.legacyloader.bridge.item.LegacyCreativeTab;
import com.myname.legacyloader.bridge.item.LegacyItemStackHelper;
import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10莠呈鋤縺ｮ繧ｹ繝ｩ繝悶ヶ繝ｭ繝・け
 * 笘・㍾隕√↑險ｭ險亥､画峩:
 * - 繝｡繧ｿ繝・・繧ｿ莉倥″繧ｹ繝ｩ繝厄ｼ医き繝ｩ繝ｼ繝舌Μ繧ｨ繝ｼ繧ｷ繝ｧ繝ｳ・峨・縲・.20.1縺ｧ縺ｯ
 *   蜷・ヰ繝ｪ繧ｨ繝ｼ繧ｷ繝ｧ繝ｳ繧貞句挨縺ｮBlockState繝励Ο繝代ユ繧｣縺ｧ縺ｯ縺ｪ縺上・
 *   ItemStack縺ｮNBT繧ｿ繧ｰ縺ｧ繝｡繧ｿ繝・・繧ｿ繧堤ｮ｡逅・☆繧・
 * - VARIANT繝励Ο繝代ユ繧｣縺ｯ菴ｿ繧上↑縺・ｼ・labBlock縺ｮTYPE/WATERLOGGED縺ｮ縺ｿ・・
 * - 縺薙ｌ縺ｫ繧医ｊ縲√せ繝ｩ繝悶・荳贋ｸ矩・鄂ｮ繝ｻ繝繝悶Ν繧ｹ繝ｩ繝也ｵ仙粋縺梧ｭ｣縺励￥蜍穂ｽ懊☆繧・
 */
public class LegacyBlockSlab extends SlabBlock implements ILegacyBlock {

    public static final List<LegacyBlockSlab> ALL_SLAB_INSTANCES = new ArrayList<>();
    public static final IntegerProperty METADATA = LegacyBlock.METADATA;

    public String legacyTextureName;
    public String legacyUnlocalizedName;
    public LegacyIcon field_149761_L;
    public final LegacyMaterial legacyMaterial;
    private LegacyCreativeTab assignedTab;

    public final boolean isDouble;
    public final boolean field_150004_a;
    protected LegacyBlockSlab pairedSlab;
    protected int maxVariants = 1;

    protected LegacyIcon[] icons = new LegacyIcon[16];

    protected Block sourceBlock;
    protected int sourceMeta;

    public LegacyBlockSlab(boolean isDouble, LegacyMaterial material) {
        super(BlockBehaviour.Properties.of()
                .mapColor(material != null ? material.getColor() : LegacyMaterial.ROCK.getColor())
                .strength(2.0F, 10.0F));
        this.legacyMaterial = material != null ? material : LegacyMaterial.ROCK;
        this.isDouble = isDouble;
        this.field_150004_a = isDouble;
        this.registerDefaultState(this.defaultBlockState().setValue(METADATA, 0));

        ALL_SLAB_INSTANCES.add(this);
        LegacyGameRegistry.trackSlabBlock(this);
    }

    public LegacyBlockSlab(boolean isDouble, Block block, int meta) {
        this(isDouble, getMaterialFromBlock(block));
        this.sourceBlock = block;
        this.sourceMeta = meta;

        if (block instanceof ILegacyBlock) {
            String texName = ((ILegacyBlock) block).getTextureName();
            if (texName != null) {
                this.legacyTextureName = texName;
            }
        }
    }

    public LegacyBlockSlab(LegacyMaterial material) {
        this(false, material);
    }

    public LegacyBlockSlab(boolean isDouble, LegacyMaterial material, String name, String textureName,
                           LegacyCreativeTab tab, LegacySoundType sound, float hardness, float resistance,
                           String toolClass, int harvestLevel) {
        this(isDouble, material);
        setBlockName(name);
        setBlockTextureName(textureName);
        if (tab != null) setCreativeTab(tab);
        setStepSound(sound);
        setHardness(hardness);
        setResistance(resistance);
        setHarvestLevel(toolClass, harvestLevel);
        setLightOpacity(isDouble ? 255 : 0);
    }

    private static LegacyMaterial getMaterialFromBlock(Block block) {
        if (block instanceof ILegacyBlock) {
            return ((ILegacyBlock) block).getMaterial();
        }
        return LegacyMaterial.ROCK;
    }

    // 笘・炎髯､: createBlockStateDefinition - VARIANT繝励Ο繝代ユ繧｣繧定ｿｽ蜉縺励↑縺・
    // SlabBlock縺ｮ繝・ヵ繧ｩ繝ｫ繝茨ｼ・YPE, WATERLOGGED・峨・縺ｿ菴ｿ逕ｨ

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(METADATA);
    }

    public void setPairedSlab(LegacyBlockSlab other) {
        this.pairedSlab = other;
        if (other != null && other.pairedSlab != this) {
            other.pairedSlab = this;
        }
    }

    public LegacyBlockSlab getPairedSlab() {
        return this.pairedSlab;
    }

    public String func_150002_b(int meta) {
        return getFullSlabName(meta);
    }

    public String getFullSlabName(int meta) {
        if (this.legacyUnlocalizedName != null) {
            return this.legacyUnlocalizedName + "_" + meta;
        }
        return null;
    }

    public LegacyBlockSlab setMaxVariants(int max) {
        this.maxVariants = Math.min(max, 8);
        return this;
    }

    public int getMaxVariants() {
        return this.maxVariants;
    }

    // === ILegacyBlock Implementation ===

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
        if (tab != null && !this.isDouble) {
            tab.addItem(this);
        }
        return this;
    }

    public LegacyCreativeTab getCreativeTab() {
        return this.assignedTab;
    }

    @Override public Block setHardness(float hardness) { return this; }
    @Override public Block setResistance(float resistance) { return this; }
    @Override public Block setLightLevel(float value) { return this; }
    @Override public Block setLightOpacity(int opacity) { return this; }
    @Override public Block setStepSound(LegacySoundType sound) { return this; }
    public Block setStepSound(Object sound) { return this; }
    @Override public void setHarvestLevel(String toolClass, int level) {}
    @Override public void setHarvestLevel(String toolClass, int level, int metadata) {}
    @Override public void registerBlockIcons(LegacyIconRegister reg) {}

    public void func_149651_a(LegacyIconRegister register) { registerBlockIcons(register); }
    public void func_149651_a(Object register) {
        if (register instanceof LegacyIconRegister) registerBlockIcons((LegacyIconRegister) register);
    }

    public LegacyIcon func_149691_a(int side, int meta) { return getIcon(side, meta); }
    public LegacyIcon func_149673_e(BlockGetter world, int x, int y, int z, int side) {
        int meta = world != null ? com.myname.legacyloader.bridge.world.LegacyWorldHelper.getBlockMetadata(world, x, y, z) : 0;
        return getIcon(side, meta);
    }
    public int func_149720_d(BlockGetter world, int x, int y, int z) { return 0xFFFFFF; }
    public int getLightValue(BlockGetter world, int x, int y, int z) { return 0; }

    public LegacyIcon getIcon(int side, int meta) {
        if (meta >= 0 && meta < icons.length && icons[meta] != null) return icons[meta];
        if (icons[0] != null) return icons[0];
        return field_149761_L;
    }

    public void func_149676_a(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}
    public void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}
    public void func_149719_a(BlockGetter world, int x, int y, int z) {}
    public AABB func_149633_g(Level world, int x, int y, int z) { return null; }
    public boolean func_149662_c() { return isDouble; }
    public boolean func_149686_d() { return isDouble; }
    public int func_149645_b() { return 0; }
    public String func_149739_a() { return legacyUnlocalizedName != null ? legacyUnlocalizedName : getDescriptionId(); }

    @Override
    public void getSubBlocks(Item item, LegacyCreativeTab tab, List<ItemStack> list) {
        if (this.isDouble) return;
        for (int i = 0; i < maxVariants; i++) {
            ItemStack stack = new ItemStack(item);
            LegacyItemStackHelper.setMetadata(stack, i);
            list.add(stack);
        }
    }

    @Override
    public int damageDropped(int meta) { return meta & 0x07; }

    @Override
    public boolean hasSubtypes() { return maxVariants > 1; }

    @Override
    public int getMaxMetadata() { return maxVariants; }

    @Override
    public String getDescriptionId() {
        if (this.legacyUnlocalizedName != null) return "tile." + this.legacyUnlocalizedName;
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

    public void func_149666_a(Item item, LegacyCreativeTab tab, List<ItemStack> list) {
        getSubBlocks(item, tab, list);
    }
    public void func_149666_a(Item item, Object tab, List<ItemStack> list) {
        LegacyCreativeTab legacyTab = tab instanceof LegacyCreativeTab ? (LegacyCreativeTab) tab : null;
        getSubBlocks(item, legacyTab, list);
    }
    public int func_149692_a(int meta) { return damageDropped(meta); }
}
