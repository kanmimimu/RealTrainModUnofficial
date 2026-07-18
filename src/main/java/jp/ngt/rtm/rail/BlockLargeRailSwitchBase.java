package jp.ngt.rtm.rail;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 本家 jp.ngt.rtm.rail.BlockLargeRailSwitchBase の忠実移植。
 * (本家の onNeighborBlockChange はコメントアウト済 = 分岐のRS判定は Point.onUpdate のポーリング)
 */
public class BlockLargeRailSwitchBase extends BlockLargeRailBase {

    public BlockLargeRailSwitchBase(int par1, Properties props) {
        super(par1, props);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityLargeRailSwitchBase(pos, state);
    }

    @Override
    public boolean isCore() {
        return false;
    }
}
