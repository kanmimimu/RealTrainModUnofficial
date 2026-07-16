package com.myname.legacyloader.bridge.forge;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.world.LegacyWorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class LegacyBlockSnapshot {
    public final Level world;
    public final int x;
    public final int y;
    public final int z;
    public final Block replacedBlock;
    public final int meta;
    public final int flag;
    public final CompoundTag nbt;

    public LegacyBlockSnapshot(Level world, int x, int y, int z, Block block, int meta) {
        this(world, x, y, z, block, meta, 3, null);
    }

    public LegacyBlockSnapshot(Level world, int x, int y, int z, Block block, int meta, CompoundTag nbt) {
        this(world, x, y, z, block, meta, 3, nbt);
    }

    public LegacyBlockSnapshot(Level world, int x, int y, int z, Block block, int meta, int flag) {
        this(world, x, y, z, block, meta, flag, null);
    }

    public LegacyBlockSnapshot(Level world, int x, int y, int z, Block block, int meta, int flag, CompoundTag nbt) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.replacedBlock = block != null ? block : Blocks.AIR;
        this.meta = Math.max(0, Math.min(15, meta));
        this.flag = flag;
        this.nbt = nbt;
    }

    public LegacyBlockSnapshot(int dimension, int x, int y, int z, String modid, String blockName, int meta, int flag, CompoundTag nbt) {
        this(null, x, y, z, Blocks.AIR, meta, flag, nbt);
    }

    public static LegacyBlockSnapshot getBlockSnapshot(Level world, int x, int y, int z) {
        Block block = world != null ? LegacyWorldHelper.getBlock(world, x, y, z) : Blocks.AIR;
        int meta = world != null ? LegacyWorldHelper.getBlockMetadata(world, x, y, z) : 0;
        return new LegacyBlockSnapshot(world, x, y, z, block, meta);
    }

    public static LegacyBlockSnapshot getBlockSnapshot(Level world, int x, int y, int z, int flag) {
        Block block = world != null ? LegacyWorldHelper.getBlock(world, x, y, z) : Blocks.AIR;
        int meta = world != null ? LegacyWorldHelper.getBlockMetadata(world, x, y, z) : 0;
        return new LegacyBlockSnapshot(world, x, y, z, block, meta, flag);
    }

    public static LegacyBlockSnapshot readFromNBT(CompoundTag tag) {
        if (tag == null) return null;
        return new LegacyBlockSnapshot(
                tag.getInt("dimension"),
                tag.getInt("posX"),
                tag.getInt("posY"),
                tag.getInt("posZ"),
                tag.getString("blockMod"),
                tag.getString("blockName"),
                tag.getInt("metadata"),
                tag.getInt("flag"),
                tag.getCompound("tileEntity"));
    }

    public Block getReplacedBlock() {
        return replacedBlock;
    }

    public boolean restore(boolean force, boolean applyPhysics) {
        if (world == null) return false;
        BlockPos pos = new BlockPos(x, y, z);
        var state = replacedBlock.defaultBlockState();
        if (state.hasProperty(LegacyBlock.METADATA)) {
            state = state.setValue(LegacyBlock.METADATA, meta);
        }
        return world.setBlock(pos, state, applyPhysics ? flag : 2);
    }
}
