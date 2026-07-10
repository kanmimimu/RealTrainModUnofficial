package jp.ngt.rtm.rail;

import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailMapBasic;
import jp.ngt.rtm.rail.util.RailPosition;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 本家 jp.ngt.rtm.rail.TileEntityLargeRailCore (KaizPatchX) の忠実移植。
 * NBT 形式は本家準拠: Property / SubRails / StartRP / EndRP / fixRTMRailMapVersion。
 * 1.21 適合: DisplayList (GL) キャッシュは BER 側で管理するため保持しない
 * (shouldRerenderRail フラグのみ維持)。レガシー railShape 互換パスは省略 (新規ワールド前提)。
 */
public abstract class TileEntityLargeRailCore extends TileEntityLargeRailBase {
    public boolean breaking;
    protected boolean isCollidedTrain = false;
    public boolean colliding = false;
    private int signal = 0;

    private RailProperty property = RailProperty.getDefaultProperty();
    public final List<RailProperty> subRails = new ArrayList<>();

    protected RailPosition[] railPositions;
    protected RailMap railmap;

    private AABB renderAABB;

    /**
     * レールを再描画するかどうか(明るさ変更等)
     */
    public boolean shouldRerenderRail;
    // see RailMapBasic.fixRTMRailMapVersion
    protected int fixRTMRailMapVersion;

    public TileEntityLargeRailCore(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);

        this.readRailProperties(nbt);
        this.readRailData(nbt);
        // 同期・再読込時にキャッシュを無効化
        this.railmap = null;
        this.shouldRerenderRail = true;
    }

    public void readRailProperties(CompoundTag nbt) {
        if (nbt.contains("Property")) {
            this.property = RailProperty.readFromNBT(nbt.getCompound("Property"));
            this.subRails.clear();
            ListTag list = nbt.getList("SubRails", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag nbt1 = list.getCompound(i);
                RailProperty property = RailProperty.readFromNBT(nbt1);
                this.subRails.add(property);
            }
        }
    }

    protected void readRailData(CompoundTag nbt) {
        this.railPositions = new RailPosition[2];
        if (nbt.contains("StartRP")) {
            this.railPositions[0] = RailPosition.readFromNBT(nbt.getCompound("StartRP"));
            this.railPositions[1] = RailPosition.readFromNBT(nbt.getCompound("EndRP"));
            this.fixRTMRailMapVersion = nbt.getInt("fixRTMRailMapVersion");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);

        this.writeRailProperties(nbt);
        this.writeRailData(nbt);
    }

    public void writeRailProperties(CompoundTag nbt) {
        CompoundTag nbtProp = new CompoundTag();
        this.property.writeToNBT(nbtProp);
        nbt.put("Property", nbtProp);
        ListTag tagList = new ListTag();
        this.subRails.forEach(property -> {
            CompoundTag nbtProp1 = new CompoundTag();
            property.writeToNBT(nbtProp1);
            tagList.add(nbtProp1);
        });
        nbt.put("SubRails", tagList);
    }

    protected void writeRailData(CompoundTag nbt) {
        if (this.railPositions != null && this.railPositions.length >= 2
                && this.railPositions[0] != null && this.railPositions[1] != null) {
            nbt.put("StartRP", this.railPositions[0].writeToNBT());
            nbt.put("EndRP", this.railPositions[1].writeToNBT());
        }
        nbt.putInt("fixRTMRailMapVersion", this.fixRTMRailMapVersion);
    }

    public void createRailMap() {
        if (this.isLoaded())//同期ができてない状態でのRailMapの生成を防ぐ
        {
            this.railmap = new RailMapBasic(this.railPositions[0], this.railPositions[1], this.fixRTMRailMapVersion);
        }
    }

    /**
     * レール情報の読み込みが完了してるかどうか(=RailPositionが存在する)
     */
    public boolean isLoaded() {
        return (this.railPositions != null && this.railPositions.length > 0 && Arrays.stream(this.railPositions).allMatch(Objects::nonNull));
    }

    public RailPosition[] getRailPositions() {
        return this.railPositions;
    }

    public void setRailPositions(RailPosition[] par1) {
        this.railPositions = par1;
    }

    public RailProperty getProperty() {
        return this.property;
    }

    public void setProperty(String s, Block block, int p3, float p4) {
        this.property = new RailProperty(s, block, p3, p4);
    }

    public void setProperty(RailProperty p1) {
        this.property = p1;
    }

    public int getSignal() {
        return this.signal;
    }

    public void setSignal(int par1) {
        this.signal = par1;
    }

    public boolean isCollidedTrain() {
        return this.isCollidedTrain;
    }

    public int getFixRTMRailMapVersion() {
        return this.fixRTMRailMapVersion;
    }

    public void setFixRTMRailMapVersion(int version) {
        this.fixRTMRailMapVersion = version;
    }

    @Override
    public TileEntityLargeRailCore getRailCore() {
        return this;
    }

    public void replaceRail(RailProperty state) {
        CompoundTag tag = new CompoundTag();
        state.writeToNBT(tag);
        this.property = RailProperty.readFromNBT(tag);
        this.subRails.clear();
        this.setChanged();
        this.markBlockForUpdate();
    }

    public void addSubRail(RailProperty state) {
        CompoundTag tag = new CompoundTag();
        state.writeToNBT(tag);
        RailProperty newState = RailProperty.readFromNBT(tag);
        RailProperty oldState = this.subRails.stream().filter(state1 -> state1.railModel.equals(newState.railModel)).findFirst().orElse(null);
        if (oldState == null) {
            if (!this.getProperty().railModel.equals(newState.railModel)) {
                this.subRails.add(newState);
            }
        } else {
            this.subRails.remove(oldState);
        }
        this.setChanged();
        this.markBlockForUpdate();
    }

    protected void markBlockForUpdate() {
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    /**
     * 本家 updateEntity 相当。Block 側の BlockEntityTicker から毎 tick 呼ばれる。
     */
    public void tick() {
        if (this.level != null && !this.level.isClientSide) {
            this.isCollidedTrain = this.colliding;
            this.colliding = false;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityLargeRailCore be) {
        be.tick();
    }

    @Override
    public RailMap getRailMap(Entity entity) {
        if (this.railmap == null) {
            this.createRailMap();
        }
        return this.railmap;
    }

    public RailMap[] getAllRailMaps() {
        RailMap rm = this.getRailMap(null);
        return rm != null ? new RailMap[]{rm} : null;
    }

    public AABB getRenderBoundingBox() {
        if (!this.isLoaded()) {
            return AABB.INFINITE;
        }

        if (this.renderAABB == null) {
            this.renderAABB = this.getRenderAABB();
            if (this.renderAABB == null) {
                return AABB.INFINITE;
            }//ぬるぽ回避
        }
        return this.renderAABB;
    }

    /**
     * レールの描画用AABBを取得<br>
     * 呼び出しは最初の1回のみ
     */
    protected AABB getRenderAABB() {
        int[] size = this.getRailSize();
        AABB aabb = new AABB(size[0] - 3.5, size[1] - 10, size[2] - 3.5, size[3] + 5.5, size[4] + 2, size[5] + 5.5);
        if (aabb.maxX - aabb.minX <= 3 && aabb.maxZ - aabb.minZ <= 3) {
            return null;
        }
        return aabb;
    }

    /**
     * {XMin, YMin, ZMin, XMax, YMax, ZMax}
     */
    public int[] getRailSize() {
        int startX = this.railPositions[0].blockX;
        int startY = this.railPositions[0].blockY;
        int startZ = this.railPositions[0].blockZ;
        int endX = this.railPositions[1].blockX;
        int endY = this.railPositions[1].blockY;
        int endZ = this.railPositions[1].blockZ;

        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        int minY = Math.min(startY, endY);
        int maxY = Math.max(startY, endY);
        int minZ = Math.min(startZ, endZ);
        int maxZ = Math.max(startZ, endZ);
        return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    /**
     * レール形状の説明を取得(アイテム表示用)
     */
    public abstract String getRailShapeName();
}
