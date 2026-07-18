package jp.ngt.rtm.electric;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 本家 jp.ngt.rtm.electric.TileEntitySignalConverter の移植 (簡略版)。
 * RSIn: レッドストーン入力→配線網 / RSOut: 配線網→レッドストーン出力 /
 * Increment・Decrement: 通過する信号レベルを ±1。
 * TODO: Wireless (無線) は未対応。
 */
public class TileEntitySignalConverter extends BlockEntity implements IProvideElectricity {
    private int electricity;
    private int prevInputSignal = -1;

    public TileEntitySignalConverter(BlockPos pos, BlockState state) {
        super(com.portofino.realtrainmodunofficial.RealTrainModUnofficialBlockEntities.SIGNAL_CONVERTER.get(), pos, state);
    }

    public SignalConverterType getConverterType() {
        return SignalConverterType.getType(this.getBlockState().getValue(BlockSignalConverter.TYPE));
    }

    @Override
    public int getElectricity() {
        return this.electricity;
    }

    @Override
    public void setElectricity(int x, int y, int z, int level) {
        if (this.electricity != level) {
            this.electricity = level;
            this.setChanged();
            if (this.level != null && !this.level.isClientSide) {
                if (this.getConverterType() == SignalConverterType.RSOut) {
                    //レッドストーン出力の更新
                    this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
                }
            }
        }
    }

    /**
     * RSIn: レッドストーン入力を監視して配線網へ伝播
     */
    public static void tick(Level level, BlockPos pos, BlockState state, TileEntitySignalConverter be) {
        if (level.isClientSide) {
            return;
        }
        if (be.getConverterType() == SignalConverterType.RSIn) {
            int signal = level.getBestNeighborSignal(pos);
            if (signal != be.prevInputSignal) {
                be.prevInputSignal = signal;
                be.electricity = signal;
                be.setChanged();
                WireManager.propagate(level, pos, signal);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("electricity", this.electricity);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.electricity = nbt.getInt("electricity");
    }
}
