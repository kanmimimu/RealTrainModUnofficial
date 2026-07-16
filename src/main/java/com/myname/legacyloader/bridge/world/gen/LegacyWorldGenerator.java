package com.myname.legacyloader.bridge.world.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Random;

/**
 * Bridge for 1.7.10 WorldGenerator base class.
 * Subclasses implement generate(World, Random, int, int, int).
 */
public abstract class LegacyWorldGenerator {

    /** Legacy API: override this to generate structures */
    public abstract boolean generate(Object world, Random random, int x, int y, int z);

    // Helper to call modern placement
    public boolean placeAt(WorldGenLevel level, RandomSource random, BlockPos pos) {
        Random rand = new Random(random.nextLong());
        return generate(level, rand, pos.getX(), pos.getY(), pos.getZ());
    }

    // SRG alias
    public boolean func_76484_a(Object world, Random random, int x, int y, int z) {
        return generate(world, random, x, y, z);
    }
}
