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

    // ---- レッドストーン出力系 (列車検知器のサーバースクリプトが置く) ----
    //
    // 1.7.10 は「1 ブロック + メタで 16 色」だったので、色付きブロックは白色版を
    // 代表として置き、メタは WorldCompat.func_147465_d が色に読み替える。

    /** redstone_block */
    public static final Block field_150451_bX = net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK;
    /** stained_glass (メタ = 色) */
    public static final Block field_150399_cn = net.minecraft.world.level.block.Blocks.WHITE_STAINED_GLASS;
    /** stained_glass_pane (メタ = 色) */
    public static final Block field_150397_co = net.minecraft.world.level.block.Blocks.WHITE_STAINED_GLASS_PANE;
    /** carpet (メタ = 色) */
    public static final Block field_150404_cg = net.minecraft.world.level.block.Blocks.WHITE_CARPET;
    /** redstone_lamp (消灯) */
    public static final Block field_150379_bu = net.minecraft.world.level.block.Blocks.REDSTONE_LAMP;
    /** redstone_torch */
    public static final Block field_150429_aA = net.minecraft.world.level.block.Blocks.REDSTONE_TORCH;
    /** lever */
    public static final Block field_150442_at = net.minecraft.world.level.block.Blocks.LEVER;
}
