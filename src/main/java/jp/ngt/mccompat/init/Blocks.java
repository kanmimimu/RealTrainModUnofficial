package jp.ngt.mccompat.init;

import net.minecraft.world.level.block.Block;

/**
 * 1.7.10 net.minecraft.init.Blocks のスクリプト互換 (SRG フィールド名)。
 * SRB3 の underBlock (羊毛) 等が参照する主要ブロックのみ。
 */
public final class Blocks {
    private Blocks() {
    }

    /** air */
    public static final Block field_150350_a = net.minecraft.world.level.block.Blocks.AIR;
    /** stone */
    public static final Block field_150348_b = net.minecraft.world.level.block.Blocks.STONE;
    /** dirt */
    public static final Block field_150346_d = net.minecraft.world.level.block.Blocks.DIRT;
    /** gravel */
    public static final Block field_150351_n = net.minecraft.world.level.block.Blocks.GRAVEL;
    /** wool */
    public static final Block field_150325_L = net.minecraft.world.level.block.Blocks.WHITE_WOOL;
    /** glass */
    public static final Block field_150359_w = net.minecraft.world.level.block.Blocks.GLASS;
    /** glowstone */
    public static final Block field_150426_aN = net.minecraft.world.level.block.Blocks.GLOWSTONE;
    /** iron_block */
    public static final Block field_150339_S = net.minecraft.world.level.block.Blocks.IRON_BLOCK;
    /** stained_hardened_clay (白色ハードクレイ相当) */
    public static final Block field_150406_ce = net.minecraft.world.level.block.Blocks.WHITE_TERRACOTTA;
}
