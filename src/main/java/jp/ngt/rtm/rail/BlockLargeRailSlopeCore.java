package jp.ngt.rtm.rail;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 勾配コアブロック (本家 BlockLargeRailSlopeCore 相当)。
 */
public class BlockLargeRailSlopeCore extends BlockLargeRailSlopeBase {

    public BlockLargeRailSlopeCore(int par1, Properties props) {
        super(par1, props);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityLargeRailSlopeCore(pos, state);
    }

    @Override
    public boolean isCore() {
        return true;
    }
}
