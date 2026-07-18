package jp.ngt.rtm.rail;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 本家 jp.ngt.rtm.rail.BlockTurntableCore の忠実移植。
 */
public class BlockTurntableCore extends BlockLargeRailBase {

    public BlockTurntableCore(int par1, Properties props) {
        super(par1, props);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityTurnTableCore(pos, state);
    }

    @Override
    public boolean isCore() {
        return true;
    }
}
