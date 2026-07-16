package com.myname.legacyloader.bridge.world;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Tiny BlockGetter used while baking 1.7.10 ISBRH custom renderers.
 * It presents one legacy block at 0,0,0 with the requested metadata and air around it.
 */
public class LegacySingleBlockAccess implements BlockGetter {
    private final Block block;
    private final int meta;
    private final boolean sameHorizontalNeighbors;
    private final boolean grassBelow;

    public LegacySingleBlockAccess(Block block, int meta) {
        this(block, meta, false, false);
    }

    public LegacySingleBlockAccess(Block block, int meta, boolean sameHorizontalNeighbors, boolean grassBelow) {
        this.block = block;
        this.meta = Math.max(0, Math.min(15, meta));
        this.sameHorizontalNeighbors = sameHorizontalNeighbors;
        this.grassBelow = grassBelow;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) { return null; }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        Block blockAt = getBlock(pos.getX(), pos.getY(), pos.getZ());
        if (blockAt != Blocks.AIR) {
            BlockState state = blockAt.defaultBlockState();
            if (blockAt == block && state.hasProperty(LegacyBlock.METADATA)) state = state.setValue(LegacyBlock.METADATA, meta);
            return state;
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }

    @Override
    public int getHeight() { return 256; }

    @Override
    public int getMinBuildHeight() { return 0; }

    public int getBlockMetadata(int x, int y, int z) { return getBlock(x, y, z) == block ? meta : 0; }
    public Block getBlock(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0 && block != null) return block;
        if (sameHorizontalNeighbors && y == 0 && block != null && isRequestedWireNeighbor(x, z)) return block;
        if (grassBelow && x == 0 && y == -1 && z == 0) return Blocks.GRASS_BLOCK;
        return Blocks.AIR;
    }

    private boolean isRequestedWireNeighbor(int x, int z) {
        if (x == -1 && z == 0) return (meta & 1) != 0;
        if (x == 1 && z == 0) return (meta & 2) != 0;
        if (x == 0 && z == -1) return (meta & 4) != 0;
        if (x == 0 && z == 1) return (meta & 8) != 0;
        return false;
    }
    public boolean isAirBlock(int x, int y, int z) { return getBlock(x, y, z) == Blocks.AIR; }
    public int getSavedLightValue(Object skyBlock, int x, int y, int z) { return 15; }
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int fallback) { return 15 << 20 | 15 << 4; }
}
