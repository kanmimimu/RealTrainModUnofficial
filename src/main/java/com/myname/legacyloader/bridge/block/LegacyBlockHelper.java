package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.client.LegacyIcon;
import com.myname.legacyloader.bridge.item.LegacyCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;

public class LegacyBlockHelper {

    private static ILegacyBlock cast(Block block) {
        return (block instanceof ILegacyBlock) ? (ILegacyBlock) block : null;
    }

    // === 蝓ｺ譛ｬ繝｡繧ｽ繝・ラ ===

    public static LegacyMaterial getMaterial(Block block) {
        ILegacyBlock lb = cast(block);
        if (lb != null) return lb.getMaterial();
        if (block == null || block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) return LegacyMaterial.AIR;
        if (block == Blocks.WATER) return LegacyMaterial.WATER;
        if (block == Blocks.LAVA) return LegacyMaterial.LAVA;
        if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE) return LegacyMaterial.FIRE;
        if (block == Blocks.GLASS || block == Blocks.GLASS_PANE) return LegacyMaterial.GLASS;
        if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK || block == Blocks.POWDER_SNOW) return LegacyMaterial.SNOW;
        if (block == Blocks.SAND || block == Blocks.RED_SAND) return LegacyMaterial.SAND;
        return LegacyMaterial.ROCK;
    }

    public static Block setBlockName(Block block, String name) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setBlockName(name);
        return block;
    }

    public static Block setBlockTextureName(Block block, String name) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setBlockTextureName(name);
        return block;
    }

    public static Block setCreativeTab(Block block, LegacyCreativeTab tab) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setCreativeTab(tab);
        return block;
    }

    public static Block setHardness(Block block, float hardness) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setHardness(hardness);
        return block;
    }

    public static Block setResistance(Block block, float resistance) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setResistance(resistance);
        return block;
    }

    public static Block setStepSound(Block block, LegacySoundType sound) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setStepSound(sound);
        return block;
    }

    public static Block setStepSound(Block block, Object sound) {
        if (sound instanceof LegacySoundType) {
            return setStepSound(block, (LegacySoundType) sound);
        }
        return block;
    }

    public static Block setLightLevel(Block block, float value) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setLightLevel(value);
        return block;
    }

    public static Block setLightOpacity(Block block, int opacity) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setLightOpacity(opacity);
        return block;
    }

    public static void setHarvestLevel(Block block, String toolClass, int level) {
        ILegacyBlock lb = cast(block);
        if (lb != null) lb.setHarvestLevel(toolClass, level);
    }

    public static Block setBlockUnbreakable(Block block) {
        if (block instanceof LegacyBlock legacyBlock) {
            legacyBlock.setHardness(-1.0F);
        }
        return block;
    }

    public static Block disableStats(Block block) {
        return block;
    }

    public static Block setTickRandomly(Block block, boolean value) {
        return block;
    }

    public static String getTextureName(Block block) {
        ILegacyBlock lb = cast(block);
        return lb != null ? lb.getTextureName() : null;
    }

    public static String func_149739_a(Block block) {
        if (block instanceof LegacyBlock legacyBlock && legacyBlock.legacyUnlocalizedName != null) {
            return "tile." + legacyBlock.legacyUnlocalizedName;
        }
        return block == null ? "" : block.getDescriptionId();
    }

    public static LegacyIcon func_149691_a(Block block, int side, int meta) {
        if (block instanceof LegacyBlock legacyBlock) return legacyBlock.getIcon(side, meta);
        if (block instanceof LegacyBlockSlab slab) return slab.getIcon(side, meta);
        if (block instanceof LegacyBlockStairs stairs) return stairs.getIcon(side, meta);
        Object value = invoke(block, "func_149691_a",
                new Class<?>[]{int.class, int.class}, side, meta);
        return value instanceof LegacyIcon ? (LegacyIcon) value : null;
    }

    public static LegacyIcon getIcon(Block block, int side, int meta) {
        return func_149691_a(block, side, meta);
    }

    public static LegacyIcon func_149733_h(Block block, int side) {
        Object value = invoke(block, "func_149733_h", new Class<?>[]{int.class}, side);
        if (value instanceof LegacyIcon icon) return icon;
        return func_149691_a(block, side, 0);
    }

    public static LegacyIcon getBlockTextureFromSide(Block block, int side) {
        return func_149733_h(block, side);
    }

    public static LegacyIcon func_149673_e(Block block, BlockGetter world, int x, int y, int z, int side) {
        Object value = invoke(block, "func_149673_e",
                new Class<?>[]{BlockGetter.class, int.class, int.class, int.class, int.class},
                world, x, y, z, side);
        if (value instanceof LegacyIcon icon) return icon;
        int meta = com.myname.legacyloader.bridge.world.LegacyWorldHelper.getBlockMetadata(world, x, y, z);
        return func_149691_a(block, side, meta);
    }

    public static int func_149720_d(Block block, BlockGetter world, int x, int y, int z) {
        Object value = invoke(block, "func_149720_d",
                new Class<?>[]{BlockGetter.class, int.class, int.class, int.class}, world, x, y, z);
        return value instanceof Number number ? number.intValue() : 0xFFFFFF;
    }

    public static int getLightValue(Block block, BlockGetter world, int x, int y, int z) {
        Object value = invoke(block, "getLightValue",
                new Class<?>[]{BlockGetter.class, int.class, int.class, int.class}, world, x, y, z);
        if (value instanceof Number number) return number.intValue();
        if (world == null) return 0;
        return block.defaultBlockState().getLightEmission(world, new BlockPos(x, y, z));
    }

    public static boolean func_149646_a(Block block, BlockGetter world, int x, int y, int z, int side) {
        Object value = invoke(block, "func_149646_a",
                new Class<?>[]{BlockGetter.class, int.class, int.class, int.class, int.class},
                world, x, y, z, side);
        if (value instanceof Boolean bool) return bool;
        return block != null && block != Blocks.AIR;
    }

    public static boolean hasTileEntity(Block block, int metadata) {
        Object value = invoke(block, "hasTileEntity", new Class<?>[]{int.class}, metadata);
        if (value instanceof Boolean bool) return bool;
        return block instanceof EntityBlock;
    }

    public static Block func_149634_a(Item item) {
        if (item == null) return Blocks.AIR;
        Block block = Block.byItem(item);
        return block == null ? Blocks.AIR : block;
    }

    public static void setBlockBounds(Block block, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (block instanceof LegacyBlock legacyBlock) {
            legacyBlock.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static Object invoke(Block block, String name, Class<?>[] types, Object... args) {
        if (block == null) return null;
        try {
            Method method = block.getClass().getMethod(name, types);
            if (method.getDeclaringClass() == LegacyBlockHelper.class) return null;
            return method.invoke(block, args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // === SRG蜷阪お繧､繝ｪ繧｢繧ｹ (蜈ｨ繝代ち繝ｼ繝ｳ邯ｲ鄒・ ===

    public static LegacyMaterial func_149688_o(Block block) { return getMaterial(block); }

    public static Block func_149663_c(Block block, String name) { return setBlockName(block, name); }

    public static Block func_149658_d(Block block, String name) { return setBlockTextureName(block, name); }

    public static Block func_149647_a(Block block, LegacyCreativeTab tab) { return setCreativeTab(block, tab); }

    public static Block func_149711_c(Block block, float v) { return setHardness(block, v); }

    public static Block func_149752_b(Block block, float v) { return setResistance(block, v); }

    // setStepSound - 荳｡譁ｹ縺ｮ繧ｷ繧ｰ繝阪メ繝｣繧堤畑諢・
    public static Block func_149672_a(Block block, LegacySoundType sound) { return setStepSound(block, sound); }
    public static Block func_149672_a(Block block, Object sound) { return setStepSound(block, sound); }

    public static Block func_149713_g(Block block, int v) { return setLightOpacity(block, v); }

    public static Block func_149715_a(Block block, float v) { return setLightLevel(block, v); }

    public static String func_149641_N(Block block) { return getTextureName(block); }

    public static Block func_149722_s(Block block) { return setBlockUnbreakable(block); }

    public static Block func_149649_H(Block block) { return disableStats(block); }

    public static Block func_149675_a(Block block, boolean value) { return setTickRandomly(block, value); }

    public static void func_149676_a(Block block, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        setBlockBounds(block, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static boolean func_149662_c(Block block) {
        Object v = invoke(block, "func_149662_c", new Class<?>[]{});
        if (v instanceof Boolean bool) return bool;
        return defaultOpaque(block);
    }
    public static boolean func_149686_d(Block block) {
        Object v = invoke(block, "func_149686_d", new Class<?>[]{});
        if (v instanceof Boolean bool) return bool;
        return defaultOpaque(block);
    }
    public static int func_149645_b(Block block) { Object v=invoke(block,"func_149645_b",new Class<?>[]{}); return v instanceof Number?((Number)v).intValue():0; }
    public static boolean func_149655_b(Block block, BlockGetter world, int x, int y, int z) { Object v=invoke(block,"func_149655_b",new Class<?>[]{BlockGetter.class,int.class,int.class,int.class},world,x,y,z); return v instanceof Boolean?(Boolean)v:true; }
    public static net.minecraft.world.phys.AABB func_149668_a(Block block, net.minecraft.world.level.Level world, int x, int y, int z) { Object v=invoke(block,"func_149668_a",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class},world,x,y,z); return v instanceof net.minecraft.world.phys.AABB?(net.minecraft.world.phys.AABB)v:null; }
    public static net.minecraft.world.phys.AABB func_149633_g(Block block, net.minecraft.world.level.Level world, int x, int y, int z) { Object v=invoke(block,"func_149633_g",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class},world,x,y,z); if(v instanceof net.minecraft.world.phys.AABB) return (net.minecraft.world.phys.AABB)v; return null; }
    public static void func_149719_a(Block block, BlockGetter world, int x, int y, int z) { invoke(block,"func_149719_a",new Class<?>[]{BlockGetter.class,int.class,int.class,int.class},world,x,y,z); }
    public static void func_149743_a(Block block, net.minecraft.world.level.Level world, int x, int y, int z, net.minecraft.world.phys.AABB mask, java.util.List list, net.minecraft.world.entity.Entity entity) { Object v=invoke(block,"func_149743_a",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class,net.minecraft.world.phys.AABB.class,java.util.List.class,net.minecraft.world.entity.Entity.class},world,x,y,z,mask,list,entity); }
    public static boolean func_149727_a(Block block, net.minecraft.world.level.Level world, int x, int y, int z, net.minecraft.world.entity.player.Player player, int side, float hitX, float hitY, float hitZ) { Object v=invoke(block,"func_149727_a",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class,net.minecraft.world.entity.player.Player.class,int.class,float.class,float.class,float.class},world,x,y,z,player,side,hitX,hitY,hitZ); return v instanceof Boolean?(Boolean)v:false; }
    public static void func_149689_a(Block block, net.minecraft.world.level.Level world, int x, int y, int z, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) { invoke(block,"func_149689_a",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class,net.minecraft.world.entity.LivingEntity.class,net.minecraft.world.item.ItemStack.class},world,x,y,z,placer,stack); }
    public static void func_149695_a(Block block, net.minecraft.world.level.Level world, int x, int y, int z, Block neighbor) { invoke(block,"func_149695_a",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class,Block.class},world,x,y,z,neighbor); }
    public static void func_149674_a(Block block, net.minecraft.world.level.Level world, int x, int y, int z, java.util.Random rand) { invoke(block,"func_149674_a",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class,java.util.Random.class},world,x,y,z,rand); }
    public static int func_149745_a(Block block, java.util.Random rand) { Object v=invoke(block,"func_149745_a",new Class<?>[]{java.util.Random.class},rand); return v instanceof Number?((Number)v).intValue():1; }
    public static net.minecraft.world.item.Item func_149650_a(Block block, int side, java.util.Random rand, int fortune) { Object v=invoke(block,"func_149650_a",new Class<?>[]{int.class,java.util.Random.class,int.class},side,rand,fortune); return v instanceof net.minecraft.world.item.Item?(net.minecraft.world.item.Item)v:net.minecraft.world.item.Item.byBlock(block); }
    public static int func_149692_a(Block block, int meta) { Object v=invoke(block,"func_149692_a",new Class<?>[]{int.class},meta); return v instanceof Number?((Number)v).intValue():0; }
    public static boolean func_149744_f(Block block) { Object v=invoke(block,"func_149744_f",new Class<?>[]{}); return v instanceof Boolean?(Boolean)v:false; }
    public static int func_149709_b(Block block, BlockGetter world, int x, int y, int z, int side) { Object v=invoke(block,"func_149709_b",new Class<?>[]{BlockGetter.class,int.class,int.class,int.class,int.class},world,x,y,z,side); return v instanceof Number?((Number)v).intValue():0; }
    public static int func_149721_r(Block block) { Object v=invoke(block,"func_149721_r",new Class<?>[]{}); return v instanceof Number?((Number)v).intValue():0; }

    public static boolean func_149637_q(Block block) { Object v=invoke(block,"func_149637_q",new Class<?>[]{}); return v instanceof Boolean?(Boolean)v:func_149730_j(block); }
    public static int func_149677_c(Block block, BlockGetter world, int x, int y, int z) { Object v=invoke(block,"func_149677_c",new Class<?>[]{BlockGetter.class,int.class,int.class,int.class},world,x,y,z); return v instanceof Number?((Number)v).intValue():15728880; }
    public static void onBlockAdded(Block block, net.minecraft.world.level.Level world, int x, int y, int z) { invoke(block,"onBlockAdded",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class},world,x,y,z); }
    public static void breakBlock(Block block, net.minecraft.world.level.Level world, int x, int y, int z, Block oldBlock, int meta) { invoke(block,"breakBlock",new Class<?>[]{net.minecraft.world.level.Level.class,int.class,int.class,int.class,Block.class,int.class},world,x,y,z,oldBlock,meta); }
    public static void func_149726_b(Block block, net.minecraft.world.level.Level world, int x, int y, int z) { onBlockAdded(block, world, x, y, z); }
    public static void func_149749_a(Block block, net.minecraft.world.level.Level world, int x, int y, int z, Block oldBlock, int meta) { breakBlock(block, world, x, y, z, oldBlock, meta); }

    public static boolean func_149730_j(Block block) { Object v=invoke(block,"func_149730_j",new Class<?>[]{}); return v instanceof Boolean?(Boolean)v:func_149662_c(block); }

    public static boolean func_149700_E(Block block) { Object v=invoke(block,"func_149700_E",new Class<?>[]{}); return v instanceof Boolean?(Boolean)v:false; }

    private static boolean defaultOpaque(Block block) {
        if (block == null) return false;
        BlockState state = block.defaultBlockState();
        return !state.isAir() && state.canOcclude();
    }


    public static int func_149682_b(Block block) {
        if (block == null) return 0;
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getId(block);
    }

    public static Block func_149683_c(int id) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.byId(id);
    }
}
