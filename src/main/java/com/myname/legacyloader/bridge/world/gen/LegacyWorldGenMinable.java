package com.myname.legacyloader.bridge.world.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class LegacyWorldGenMinable {
    private final Block oreBlock;
    private final int numberOfBlocks;
    private final Block targetBlock;

    public LegacyWorldGenMinable(Block block, int meta, int number, Block target) {
        this.oreBlock = block;
        this.numberOfBlocks = number;
        this.targetBlock = target;
    }

    public LegacyWorldGenMinable(Block block, int number) {
        this(block, 0, number, Blocks.STONE);
    }

    public boolean func_76484_a(Level world, Random random, int x, int y, int z) {
        return generate(world, random, x, y, z);
    }

    public boolean generate(Level world, Random random, int x, int y, int z) {
        float f = random.nextFloat() * (float)Math.PI;
        double d0 = (double)((float)(x + 8) + Math.sin(f) * (float)this.numberOfBlocks / 8.0F);
        double d1 = (double)((float)(x + 8) - Math.sin(f) * (float)this.numberOfBlocks / 8.0F);
        double d2 = (double)((float)(z + 8) + Math.cos(f) * (float)this.numberOfBlocks / 8.0F);
        double d3 = (double)((float)(z + 8) - Math.cos(f) * (float)this.numberOfBlocks / 8.0F);
        double d4 = (double)(y + random.nextInt(3) - 2);
        double d5 = (double)(y + random.nextInt(3) - 2);

        for(int l = 0; l < this.numberOfBlocks; ++l) {
            float f1 = (float)l / (float)this.numberOfBlocks;
            double d6 = d0 + (d1 - d0) * (double)f1;
            double d7 = d4 + (d5 - d4) * (double)f1;
            double d8 = d2 + (d3 - d2) * (double)f1;
            double d9 = random.nextDouble() * (double)this.numberOfBlocks / 16.0D;
            double d10 = (double)(Math.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0D;
            double d11 = (double)(Math.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0D;
            int i1 = (int)Math.floor(d6 - d10 / 2.0D);
            int j1 = (int)Math.floor(d7 - d11 / 2.0D);
            int k1 = (int)Math.floor(d8 - d10 / 2.0D);
            int l1 = (int)Math.floor(d6 + d10 / 2.0D);
            int i2 = (int)Math.floor(d7 + d11 / 2.0D);
            int j2 = (int)Math.floor(d8 + d10 / 2.0D);

            for(int k2 = i1; k2 <= l1; ++k2) {
                double d12 = ((double)k2 + 0.5D - d6) / (d10 / 2.0D);
                if (d12 * d12 < 1.0D) {
                    for(int l2 = j1; l2 <= i2; ++l2) {
                        double d13 = ((double)l2 + 0.5D - d7) / (d11 / 2.0D);
                        if (d12 * d12 + d13 * d13 < 1.0D) {
                            for(int i3 = k1; i3 <= j2; ++i3) {
                                double d14 = ((double)i3 + 0.5D - d8) / (d10 / 2.0D);
                                if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    BlockPos pos = new BlockPos(k2, l2, i3);
                                    if (world.getBlockState(pos).is(targetBlock)) {
                                        world.setBlock(pos, oreBlock.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}