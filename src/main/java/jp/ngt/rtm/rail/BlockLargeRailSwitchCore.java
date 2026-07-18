package jp.ngt.rtm.rail;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 分岐コアブロック (本家では BlockLargeRailCore のバリアント登録)。
 */
public class BlockLargeRailSwitchCore extends BlockLargeRailBase {

    public BlockLargeRailSwitchCore(int par1, Properties props) {
        super(par1, props);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityLargeRailSwitchCore(pos, state);
    }

    @Override
    public boolean isCore() {
        return true;
    }
}
