package com.myname.legacyloader.bridge.world;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.tileentity.LegacyTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyWorldHelper {
    public static LegacyWorldProvider getDimensionProvider(Level world) {
        if (world == null) return null;
        ResourceKey<Level> dim = world.dimension();
        if (dim.equals(Level.OVERWORLD)) return LegacyWorldProviderSurface.INSTANCE;
        return null;
    }

    public static Block getBlock(BlockGetter world, int x, int y, int z) {
        if (world == null) return Blocks.AIR;
        return world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static int getBlockMetadata(BlockGetter world, int x, int y, int z) {
        if (world == null) return 0;
        BlockState state = world.getBlockState(new BlockPos(x, y, z));
        if (state.hasProperty(LegacyBlock.METADATA)) {
            return state.getValue(LegacyBlock.METADATA);
        }
        return 0;
    }

    public static boolean setBlockMetadata(Level world, int x, int y, int z, int metadata, int flags) {
        if (world == null) return false;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);
        if (state.hasProperty(LegacyBlock.METADATA)) {
            return world.setBlock(pos, state.setValue(LegacyBlock.METADATA, clampMeta(metadata)), flags);
        }
        return false;
    }

    public static boolean setBlock(Level world, int x, int y, int z, Block block, int metadata, int flags) {
        if (world == null || block == null) return false;
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(LegacyBlock.METADATA)) {
            state = state.setValue(LegacyBlock.METADATA, clampMeta(metadata));
        }
        return world.setBlock(new BlockPos(x, y, z), state, flags);
    }

    public static boolean canPlaceEntityOnSide(Level world, Block block, int x, int y, int z,
                                               boolean ignoreEntities, int side, Entity entity, ItemStack stack) {
        if (world == null || block == null) return false;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState existing = world.getBlockState(pos);
        if (!existing.canBeReplaced()) return false;
        try {
            BlockState state = block.defaultBlockState();
            return state.canSurvive(world, pos) && (ignoreEntities || world.isUnobstructed(state, pos, net.minecraft.world.phys.shapes.CollisionContext.empty()));
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static boolean isAirBlock(BlockGetter world, int x, int y, int z) {
        return world == null || world.getBlockState(new BlockPos(x, y, z)).isAir();
    }

    public static BlockEntity getTileEntity(BlockGetter world, int x, int y, int z) {
        return world != null ? world.getBlockEntity(new BlockPos(x, y, z)) : null;
    }

    public static LegacyTileEntity getLegacyTileEntity(BlockGetter world, int x, int y, int z) {
        BlockEntity tile = getTileEntity(world, x, y, z);
        if (tile instanceof LegacyTileEntity legacyTile) {
            legacyTile.field_145851_c = x;
            legacyTile.field_145848_d = y;
            legacyTile.field_145849_e = z;
            if (world instanceof Level level) {
                legacyTile.field_145850_b = level;
            }
            return legacyTile;
        }
        return null;
    }

    public static boolean setBlockToAir(Level world, int x, int y, int z) {
        return world != null && world.removeBlock(new BlockPos(x, y, z), false);
    }

    public static void markBlockForUpdate(Level world, int x, int y, int z) {
        if (world == null) return;
        BlockPos pos = new BlockPos(x, y, z);
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    public static void notifyBlockChange(Level world, int x, int y, int z, Block block) {
        if (world == null) return;
        BlockPos pos = new BlockPos(x, y, z);
        world.updateNeighborsAt(pos, block != null ? block : world.getBlockState(pos).getBlock());
    }

    public static void scheduleBlockUpdate(Level world, int x, int y, int z, Block block, int ticks) {
        if (world == null || block == null) return;
        world.scheduleTick(new BlockPos(x, y, z), block, Math.max(0, ticks));
    }

    public static int getBlockLightValue(BlockGetter world, int x, int y, int z) {
        if (world == null) return 0;
        BlockPos pos = new BlockPos(x, y, z);
        Integer raw = invokeInt(world, "getRawBrightness", new Class<?>[]{BlockPos.class, int.class}, new Object[]{pos, 0});
        if (raw != null) return raw;

        // BlockGetter in 1.21.1 does not expose raw brightness. As a safe fallback, use
        // the block state's own light emission when available. This is not a full 1.7.10
        // lighting model, but it avoids a hard compile/runtime dependency on Level methods.
        Integer emission = invokeInt(world.getBlockState(pos), "getLightEmission", new Class<?>[]{}, new Object[]{});
        return emission != null ? emission : 0;
    }

    public static int getSavedLightValue(BlockGetter world, Object enumSkyBlock, int x, int y, int z) {
        if (world == null) return 0;
        BlockPos pos = new BlockPos(x, y, z);

        // 1.7.10 EnumSkyBlock.Sky/Block is approximated here. Use reflection so this
        // bridge compiles against both minimal BlockGetter call sites and full Level instances.
        Integer raw = invokeInt(world, "getRawBrightness", new Class<?>[]{BlockPos.class, int.class}, new Object[]{pos, 0});
        if (raw != null) return raw;

        Integer emission = invokeInt(world.getBlockState(pos), "getLightEmission", new Class<?>[]{}, new Object[]{});
        return emission != null ? emission : 0;
    }

    public static int getLightBrightnessForSkyBlocks(BlockGetter world, int x, int y, int z, int fallback) {
        if (world == null) return 0;
        if (world instanceof LegacySingleBlockAccess access) {
            return access.getLightBrightnessForSkyBlocks(x, y, z, fallback);
        }

        BlockPos pos = new BlockPos(x, y, z);
        int block = Math.max(0, fallback);
        int sky = Math.max(0, fallback);
        if (world instanceof Level level) {
            try {
                block = Math.max(block, level.getBrightness(LightLayer.BLOCK, pos));
                sky = Math.max(sky, level.getBrightness(LightLayer.SKY, pos));
            } catch (RuntimeException ignored) {
            }
        } else {
            Integer raw = invokeInt(world, "getRawBrightness", new Class<?>[]{BlockPos.class, int.class}, new Object[]{pos, fallback});
            if (raw != null) {
                block = Math.max(block, raw);
                sky = Math.max(sky, raw);
            }
        }

        block = Math.max(0, Math.min(15, block));
        sky = Math.max(0, Math.min(15, sky));
        return (sky << 20) | (block << 4);
    }

    public static boolean isRemote(Level world) {
        return world != null && world.isClientSide;
    }

    public static int getSkylightSubtracted(Level world) {
        return 0;
    }

    public static float getCelestialAngle(Level world, float partialTicks) {
        if (world == null) return 0.0F;
        return world.getTimeOfDay(partialTicks) * ((float)Math.PI * 2.0F);
    }

    public static boolean canBlockSeeTheSky(BlockGetter world, int x, int y, int z) {
        if (world == null) return false;
        BlockPos pos = new BlockPos(x, y, z);
        Boolean visible = invokeBoolean(world, "canSeeSky", new Class<?>[]{BlockPos.class}, new Object[]{pos});
        return visible != null && visible;
    }

    public static void playSoundEffect(Level world, double x, double y, double z, String sound, float volume, float pitch) {
    }

    public static void setTileEntity(Level world, int x, int y, int z, Object tile) {
        if (world == null || !(tile instanceof BlockEntity be)) return;
        if (tile instanceof LegacyTileEntity legacyTile) {
            BlockPos pos = new BlockPos(x, y, z);
            legacyTile.bindToModernBlock(world, pos, world.getBlockState(pos));
        }
        world.setBlockEntity(be);
    }

    public static void removeTileEntity(Level world, int x, int y, int z) {
        if (world != null) world.removeBlockEntity(new BlockPos(x, y, z));
    }

    public static boolean spawnEntityInWorld(Level world, Entity entity) {
        if (world == null || entity == null) return false;
        if (entity instanceof com.myname.legacyloader.bridge.entity.LegacyEntityArrow arrow) {
            arrow.syncLegacyState();
        }
        return world.addFreshEntity(entity);
    }

    public static boolean func_72838_d(Level world, Entity entity) {
        return spawnEntityInWorld(world, entity);
    }


    private static Integer invokeInt(Object target, String name, Class<?>[] types, Object[] args) {
        Object value = invoke(target, name, types, args);
        return value instanceof Number number ? number.intValue() : null;
    }

    private static Boolean invokeBoolean(Object target, String name, Class<?>[] types, Object[] args) {
        Object value = invoke(target, name, types, args);
        return value instanceof Boolean bool ? bool : null;
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object[] args) {
        if (target == null) return null;
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(name, types);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static int clampMeta(int metadata) {
        return Math.max(0, Math.min(15, metadata));
    }
}
