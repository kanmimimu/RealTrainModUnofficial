package jp.kaiz.atsassistmod.block.entity;

import com.portofino.realtrainmodunofficial.entity.TrainEntity;
import jp.kaiz.atsassistmod.ifttt.IFTTTContainer;
import jp.kaiz.atsassistmod.ifttt.IFTTTUtil;
import jp.kaiz.atsassistmod.registry.ATSAModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * IFTTT block entity (port of TileEntityIFTTT). Holds the THIS (condition) and THAT
 * (action) rule lists, evaluates them each server tick against the train on top of
 * the block, and runs the actions.
 */
public class IftttBlockEntity extends BlockEntity {
    private int redStoneOutput;
    private boolean notFirst;
    private boolean anyMatch;
    private List<IFTTTContainer> thisList = new ArrayList<>();
    private List<IFTTTContainer> thatList = new ArrayList<>();

    public IftttBlockEntity(BlockPos pos, BlockState state) {
        super(ATSAModBlockEntities.IFTTT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IftttBlockEntity be) {
        be.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide || thisList.isEmpty() || thatList.isEmpty()) {
            return;
        }
        AABB detect = new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1,
                worldPosition.getX() + 2, worldPosition.getY() + 4, worldPosition.getZ() + 2);
        List<TrainEntity> trains = level.getEntitiesOfClass(TrainEntity.class, detect);
        TrainEntity train = trains.isEmpty() ? null : trains.get(0);

        boolean match = anyMatch
                ? thisList.stream().anyMatch(c -> ((IFTTTContainer.This) c).isCondition(this, train))
                : thisList.stream().allMatch(c -> ((IFTTTContainer.This) c).isCondition(this, train));

        if (match) {
            boolean first = !notFirst;
            for (IFTTTContainer c : thatList) {
                ((IFTTTContainer.That) c).doThat(this, train, first);
            }
            notFirst = true;
        } else if (notFirst) {
            for (IFTTTContainer c : thatList) {
                ((IFTTTContainer.That) c).finish(this, train);
            }
            setRedStoneOutput(0);
            notFirst = false;
        }
    }

    public int getRedStoneOutput() {
        return redStoneOutput;
    }

    public void setRedStoneOutput(int power) {
        if (this.redStoneOutput != power) {
            this.redStoneOutput = power;
            setChanged();
            if (level != null) {
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
    }

    public boolean isAnyMatch() { return anyMatch; }
    public void setAnyMatch(boolean anyMatch) { this.anyMatch = anyMatch; }
    public List<IFTTTContainer> getThisList() { return thisList; }
    public List<IFTTTContainer> getThatList() { return thatList; }

    public void addIFTTT(IFTTTContainer c) {
        if (c instanceof IFTTTContainer.This) {
            if (thisList.size() < 6) thisList.add(c);
        } else if (c instanceof IFTTTContainer.That) {
            if (thatList.size() < 6) thatList.add(c);
        }
    }

    public void setIFTTT(IFTTTContainer c, int index) {
        if (c instanceof IFTTTContainer.This) {
            if (thisList.size() > index) thisList.set(index, c); else addIFTTT(c);
        } else if (c instanceof IFTTTContainer.That) {
            if (thatList.size() > index) thatList.set(index, c); else addIFTTT(c);
        }
    }

    public void removeIFTTT(IFTTTContainer c, int index) {
        if (c instanceof IFTTTContainer.This) {
            if (index >= 0 && index < thisList.size()) thisList.remove(index);
        } else if (c instanceof IFTTTContainer.That) {
            if (index >= 0 && index < thatList.size()) thatList.remove(index);
        }
    }

    public void replaceLists(List<IFTTTContainer> newThis, List<IFTTTContainer> newThat, boolean anyMatch) {
        this.thisList = new ArrayList<>(newThis);
        this.thatList = new ArrayList<>(newThat);
        this.anyMatch = anyMatch;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ----------------------------------------------------------------- NBT
    private static ListTag saveList(List<IFTTTContainer> list) {
        ListTag tag = new ListTag();
        for (IFTTTContainer c : list) {
            byte[] bytes = IFTTTUtil.toBytes(c);
            if (bytes != null) {
                CompoundTag entry = new CompoundTag();
                entry.putByteArray("data", bytes);
                tag.add(entry);
            }
        }
        return tag;
    }

    private static List<IFTTTContainer> loadList(ListTag tag) {
        List<IFTTTContainer> list = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) {
            IFTTTContainer c = IFTTTUtil.fromBytes(tag.getCompound(i).getByteArray("data"));
            if (c != null) {
                list.add(c);
            }
        }
        return list;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("redStoneOutput", redStoneOutput);
        tag.putBoolean("notFirst", notFirst);
        tag.putBoolean("anyMatch", anyMatch);
        tag.put("iftttThisList", saveList(thisList));
        tag.put("iftttThatList", saveList(thatList));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        redStoneOutput = tag.getInt("redStoneOutput");
        notFirst = tag.getBoolean("notFirst");
        anyMatch = tag.getBoolean("anyMatch");
        thisList = loadList(tag.getList("iftttThisList", Tag.TAG_COMPOUND));
        thatList = loadList(tag.getList("iftttThatList", Tag.TAG_COMPOUND));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
