package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.tileentity.LegacyTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Bridge for 1.7.10 BlockContainer - blocks that have TileEntities.
 * Extends LegacyBlock and implements EntityBlock so Minecraft knows this block
 * creates a BlockEntity. Delegates to LegacyITileEntityProvider when the
 * subclass (legacy mod code) implements that interface.
 */
public class LegacyContainerBlock extends LegacyBlock implements EntityBlock, LegacyITileEntityProvider {

    public LegacyContainerBlock(LegacyMaterial material) {
        super(material);
    }

    public LegacyContainerBlock() {
        super(LegacyMaterial.ROCK);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            int meta = state.hasProperty(METADATA) ? state.getValue(METADATA) : 0;
            LegacyTileEntity te = createNewTileEntity(null, meta);
            if (te != null) {
                te.bindToModernBlock(null, pos, state);
                return te;
            }
        } catch (Throwable t) {
            System.err.println("LegacyLoader: Error creating TileEntity for " + getClass().getName() + ": " + t.getMessage());
        }
        return null;
    }

    @Override
    @Nullable
    public LegacyTileEntity createNewTileEntity(net.minecraft.world.level.Level world, int meta) {
        return func_149915_a(world, meta);
    }

    @Nullable
    public LegacyTileEntity func_149915_a(net.minecraft.world.level.Level world, int meta) {
        return null;
    }

    // Legacy method: hasTileEntity(int metadata)
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    // SRG alias
    public boolean func_149675_a(int metadata) {
        return hasTileEntity(metadata);
    }
}
