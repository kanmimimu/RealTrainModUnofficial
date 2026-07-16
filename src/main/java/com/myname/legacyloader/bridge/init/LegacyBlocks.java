package com.myname.legacyloader.bridge.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class LegacyBlocks {
    public static final Block field_150350_a = Blocks.AIR;
    public static final Block field_150348_b = Blocks.STONE;
    public static final Block field_150349_c = Blocks.GRASS_BLOCK;
    public static final Block field_150346_d = Blocks.DIRT;
    public static final Block field_150347_e = Blocks.COBBLESTONE;
    public static final Block field_150344_f = Blocks.OAK_PLANKS;
    public static final Block field_150345_g = Blocks.BEDROCK;
    public static final Block field_150355_j = Blocks.WATER;
    public static final Block field_150353_l = Blocks.LAVA;
    public static final Block field_150354_m = Blocks.SAND;
    public static final Block field_150351_n = Blocks.GRAVEL;
    public static final Block field_150352_o = Blocks.GOLD_ORE;
    public static final Block field_150366_p = Blocks.IRON_ORE;
    public static final Block field_150365_q = Blocks.COAL_ORE;
    public static final Block field_150364_r = Blocks.OAK_LOG;
    public static final Block field_150363_s = Blocks.OAK_LEAVES;
    public static final Block field_150369_x = Blocks.LAPIS_ORE;
    public static final Block field_150368_y = Blocks.LAPIS_BLOCK;
    public static final Block field_150359_w = Blocks.GLASS;
    public static final Block field_150322_A = Blocks.SANDSTONE;
    public static final Block field_150329_H = Blocks.SANDSTONE;
    public static final Block field_150321_G = Blocks.BRICKS;
    public static final Block field_150337_Q = Blocks.TNT;
    public static final Block field_150338_P = Blocks.BOOKSHELF;
    public static final Block field_150331_J = Blocks.PISTON;
    public static final Block field_150332_K = Blocks.PISTON_HEAD;
    public static final Block field_150333_U = Blocks.STONE_SLAB;
    public static final Block field_150341_Y = Blocks.MOSSY_COBBLESTONE;
    public static final Block field_150343_Z = Blocks.OBSIDIAN;
    public static final Block field_150417_aV = Blocks.STONE_BRICKS;
    public static final Block field_150432_aD = Blocks.ICE;
    public static final Block field_150433_aE = Blocks.SNOW;
    public static final Block field_150434_aF = Blocks.CACTUS;
    public static final Block field_150436_aH = Blocks.SUGAR_CANE;
    public static final Block field_150403_cj = Blocks.PACKED_ICE;
    public static final Block field_150424_aL = Blocks.TERRACOTTA;
    public static final Block field_150425_aM = Blocks.COAL_BLOCK;
    public static final Block field_150378_br = Blocks.MELON;
    public static final Block field_150362_t = Blocks.OAK_LEAVES;
    public static final Block field_150361_u = Blocks.SPRUCE_LEAVES;
    public static final Block field_150428_aP = Blocks.CACTUS;
    public static final Block field_150374_bv = Blocks.TERRACOTTA;
    public static final Block field_150474_ac = Blocks.GLASS_PANE;
    public static final Block field_150486_ae = Blocks.CHEST;
    public static final Block field_150462_ai = Blocks.CRAFTING_TABLE;
    public static final Block field_150435_aG = Blocks.CLAY;
    public static final Block field_150418_aU = Blocks.IRON_BARS;
    public static final Block field_150379_bu = Blocks.MELON_STEM;
    public static final Block field_150334_T = Blocks.FIRE;
    public static final Block field_150373_bw = Blocks.PUMPKIN_STEM;
    public static final Block field_150360_v = Blocks.SPONGE;
    public static final Block field_150398_cm = Blocks.SUNFLOWER;
    public static final Block field_150395_bd = Blocks.VINE;
    public static final Block field_150480_ab = Blocks.FIRE;
    public static final Block field_150457_bL = Blocks.FLOWER_POT;
    public static final Block field_150458_ak = Blocks.FARMLAND;
    public static final Block field_150464_aj = Blocks.WHEAT;
    public static final Block field_150393_bb = Blocks.PUMPKIN_STEM;
    public static final Block field_150394_bc = Blocks.MELON_STEM;
    public static final Block field_150388_bm = Blocks.NETHER_WART;

    // 笘・ｿｮ豁｣: WOOL -> WHITE_WOOL
    public static final Block field_150325_L = Blocks.WHITE_WOOL;

    private static final java.util.Map<String, Block> FIELD_MAP = buildFieldMap();

    private static java.util.Map<String, Block> buildFieldMap() {
        java.util.Map<String, Block> map = new java.util.HashMap<>();
        for (java.lang.reflect.Field f : LegacyBlocks.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && Block.class.isAssignableFrom(f.getType())) {
                try { map.put(f.getName(), (Block) f.get(null)); } catch (Exception ignored) {}
            }
        }
        return map;
    }

    public static Block getByField(String name) {
        return FIELD_MAP.getOrDefault(name, Blocks.AIR);
    }
}
