package com.myname.legacyloader.bridge.tileentity;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.block.LegacyITileEntityProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class LegacyTileEntity extends BlockEntity {
    private static final java.lang.reflect.Field BLOCK_ENTITY_POS = findBlockEntityField("worldPosition");
    private static final java.lang.reflect.Field BLOCK_ENTITY_STATE = findBlockEntityField("blockState");

    /**
     * Shared BlockEntityType for all legacy TileEntities.
     * Created eagerly; must be registered during RegisterEvent for BLOCK_ENTITY_TYPE.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final BlockEntityType<LegacyTileEntity> LEGACY_TYPE =
            new BlockEntityType<>(LegacyTileEntity::createForBlock, java.util.Collections.emptySet(), null) {
                @Override
                public boolean isValid(BlockState state) {
                    return true;
                }
            };

    public static final Map<String, Class<? extends LegacyTileEntity>> nameToClassMap = new HashMap<>();
    public static final Map<Class<? extends LegacyTileEntity>, String> classToNameMap = new HashMap<>();
    public static final Map<String, Class<? extends LegacyTileEntity>> field_145855_i = nameToClassMap;
    public static final Map<Class<? extends LegacyTileEntity>, String> field_145853_j = classToNameMap;

    private static LegacyTileEntity createForBlock(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LegacyITileEntityProvider provider) {
            try {
                int meta = state.hasProperty(LegacyBlock.METADATA) ? state.getValue(LegacyBlock.METADATA) : 0;
                LegacyTileEntity te = provider.createNewTileEntity(null, meta);
                if (te != null) {
                    te.bindToModernBlock(null, pos, state);
                    return te;
                }
            } catch (Throwable ignored) {}
        }
        return new LegacyTileEntity(pos, state);
    }

    // Legacy coordinate fields used by 1.7.10 code
    public Level field_145850_b;
    public int field_145851_c;  // x
    public int field_145848_d;  // y
    public int field_145849_e;  // z
    public int field_145847_g = -1; // blockMetadata
    public Block field_145854_h; // blockType

    /** Modern constructor called by EntityBlock / world loading */
    public LegacyTileEntity(BlockPos pos, BlockState state) {
        super(LEGACY_TYPE, pos, state);
        this.field_145851_c = pos.getX();
        this.field_145848_d = pos.getY();
        this.field_145849_e = pos.getZ();
        this.field_145847_g = state.hasProperty(LegacyBlock.METADATA) ? state.getValue(LegacyBlock.METADATA) : 0;
        this.field_145854_h = state.getBlock();
    }

    /** Legacy no-args constructor used by legacy mod code */
    public LegacyTileEntity() {
        super(LEGACY_TYPE, BlockPos.ZERO, Blocks.AIR.defaultBlockState());
    }

    private static java.lang.reflect.Field findBlockEntityField(String name) {
        try {
            java.lang.reflect.Field field = BlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public void bindToModernBlock(Level world, BlockPos pos, BlockState state) {
        if (pos == null || state == null) return;
        try {
            if (BLOCK_ENTITY_POS != null) BLOCK_ENTITY_POS.set(this, pos.immutable());
            if (BLOCK_ENTITY_STATE != null) BLOCK_ENTITY_STATE.set(this, state);
        } catch (Throwable ignored) {
        }
        this.field_145851_c = pos.getX();
        this.field_145848_d = pos.getY();
        this.field_145849_e = pos.getZ();
        this.field_145847_g = state.hasProperty(LegacyBlock.METADATA) ? state.getValue(LegacyBlock.METADATA) : 0;
        this.field_145854_h = state.getBlock();
        if (world != null) setWorldObj(world);
    }

    public static void addMapping(Class<? extends LegacyTileEntity> tileClass, String id) {
        if (tileClass == null || id == null || id.isBlank()) return;
        nameToClassMap.put(id, tileClass);
        classToNameMap.put(tileClass, id);
    }

    public static void func_145826_a(Class<? extends LegacyTileEntity> tileClass, String id) {
        addMapping(tileClass, id);
    }

    public static LegacyTileEntity createAndLoadEntity(CompoundTag tag) {
        if (tag == null) return null;
        Class<? extends LegacyTileEntity> tileClass = nameToClassMap.get(tag.getString("id"));
        if (tileClass == null) return null;
        try {
            LegacyTileEntity tile = tileClass.getDeclaredConstructor().newInstance();
            tile.readFromNBT(tag);
            return tile;
        } catch (Throwable t) {
            return null;
        }
    }

    public static LegacyTileEntity func_145827_c(CompoundTag tag) {
        return createAndLoadEntity(tag);
    }

    // ─── Level access ───────────────────────────────────────────────────────

    public Level getWorldObj() {
        return field_145850_b != null ? field_145850_b : getLevel();
    }

    public Level func_145831_w() { return getWorldObj(); }

    public Block getBlockType() {
        if (field_145854_h == null) {
            Level world = getWorldObj();
            field_145854_h = world != null ? world.getBlockState(getBlockPos()).getBlock() : Blocks.AIR;
        }
        return field_145854_h;
    }

    public Block func_145838_q() { return getBlockType(); }

    public void setWorldObj(Level world) {
        this.field_145850_b = world;
        if (world != null) {
            BlockPos p = getBlockPos();
            this.field_145851_c = p.getX();
            this.field_145848_d = p.getY();
            this.field_145849_e = p.getZ();
            BlockState state = world.getBlockState(p);
            this.field_145854_h = state.getBlock();
            this.field_145847_g = state.hasProperty(LegacyBlock.METADATA) ? state.getValue(LegacyBlock.METADATA) : 0;
        }
    }

    public void func_145834_a(Level world) { setWorldObj(world); }

    public boolean hasWorldObj() { return getWorldObj() != null; }
    public boolean func_145830_o() { return hasWorldObj(); }

    // ─── Dirty / change notification ────────────────────────────────────────

    public void markDirty() { setChanged(); }
    public void func_70296_d() { markDirty(); }

    // ─── Coordinates (legacy API) ────────────────────────────────────────────

    public int xCoord() { return field_145851_c; }
    public int yCoord() { return field_145848_d; }
    public int zCoord() { return field_145849_e; }

    public int getBlockMetadata() {
        if (field_145847_g < 0) {
            Level world = getWorldObj();
            if (world != null) {
                BlockState state = world.getBlockState(getBlockPos());
                field_145847_g = state.hasProperty(LegacyBlock.METADATA) ? state.getValue(LegacyBlock.METADATA) : 0;
            } else {
                field_145847_g = 0;
            }
        }
        return field_145847_g;
    }

    public int func_145832_p() { return getBlockMetadata(); }

    // ─── NBT ────────────────────────────────────────────────────────────────

    /** Called by legacy mod code to write data */
    public void writeToNBT(CompoundTag tag) {
        String id = classToNameMap.get(getClass());
        if (id != null) tag.putString("id", id);
        tag.putInt("x", field_145851_c);
        tag.putInt("y", field_145848_d);
        tag.putInt("z", field_145849_e);
        tag.putInt("Meta", getBlockMetadata());
    }

    /** Called by legacy mod code to read data */
    public void readFromNBT(CompoundTag tag) {
        if (tag == null) return;
        this.field_145851_c = tag.getInt("x");
        this.field_145848_d = tag.getInt("y");
        this.field_145849_e = tag.getInt("z");
        if (tag.contains("Meta")) {
            this.field_145847_g = tag.getInt("Meta");
        }
    }

    // SRG aliases
    public CompoundTag func_145841_b(CompoundTag tag) { writeToNBT(tag); return tag; }
    public void func_145839_a(CompoundTag tag) { readFromNBT(tag); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider providers) {
        super.saveAdditional(tag, providers);
        writeToNBT(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider providers) {
        super.loadAdditional(tag, providers);
        readFromNBT(tag);
    }

    // ─── Tick support ────────────────────────────────────────────────────────

    /** Called each game tick if the block uses a ticker. Override in subclasses. */
    public void updateEntity() {}

    public void func_73660_a() { updateEntity(); }

    // ─── Misc legacy API ────────────────────────────────────────────────────

    public boolean isInvalid() {
        return isRemoved();
    }

    public void func_145843_s() { setRemoved(); }

    public String getLegacyBlockTypeName() {
        return getClass().getSimpleName();
    }
}
