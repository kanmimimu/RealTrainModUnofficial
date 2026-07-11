package jp.ngt.rtm.rail;

import com.mojang.serialization.MapCodec;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * 本家 jp.ngt.rtm.rail.BlockLargeRailBase (KaizPatchX) の忠実移植。
 * 道床(レールベース)ブロック。描画はコア BE のレンダラが行うため INVISIBLE。
 * 当たり判定は BE の getBlockHeights (4隅カント) の平均高さによる動的 VoxelShape (本家準拠)。
 */
public class BlockLargeRailBase extends BaseEntityBlock {
    public static final MapCodec<BlockLargeRailBase> CODEC = simpleCodec(props -> new BlockLargeRailBase(2, props));

    public static final float THICKNESS = 0.0625F;
    /**
     * 2:Gravel, 3:Stone, 4:Snow, 5:Asphalt
     */
    public final int railTextureType;

    public BlockLargeRailBase(int par1, Properties props) {
        super(props
                .mapColor(MapColor.STONE)
                .strength(1.0F, 15.0F)
                .sound(par1 == 2 ? SoundType.GRAVEL : (par1 == 4 ? SoundType.SNOW : SoundType.STONE))
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .dynamicShape());
        this.railTextureType = par1;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        //道床の見た目はレールモデル (MQO の base/ballast/sleeper グループ) が描画する。
        //ブロック自体は当たり判定/構造用 (静的スラブ描画は浮き・二重描画になるため廃止)。
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityLargeRailBase(pos, state);
    }

    public boolean isCore() {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getRailShape(level, pos, false);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean preventMob = false;
        if (context instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Mob) {
            preventMob = this.preventMobMovement(level, pos);
        }
        return this.getRailShape(level, pos, preventMob);
    }

    protected VoxelShape getRailShape(BlockGetter level, BlockPos pos, boolean preventMob) {
        BlockEntity be = level.getBlockEntity(pos);
        float height2 = THICKNESS;
        if (be instanceof TileEntityLargeRailBase rail) {
            float[] fa = rail.getBlockHeights(pos.getX(), pos.getY(), pos.getZ(), THICKNESS, true);
            float sum = 0.0F;
            for (int i = 0; i < 4; ++i) {
                sum += fa[i];
            }
            height2 = sum * 0.25F;
            if (height2 < THICKNESS) {
                height2 = THICKNESS;
            }
        }
        double top = preventMob ? height2 + 256.0D : height2;
        return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, top, 1.0D);
    }

    /**
     * 本家: ModelSetRail.getConfig().allowCrossing が false なら Mob 通行禁止。
     * TODO(Phase 4): ModelSetRail 移植後に allowCrossing を参照。それまでは通行許可。
     */
    public boolean preventMobMovement(BlockGetter level, BlockPos pos) {
        return false;
    }

    /**
     * 本家 getPickBlock: レールをピックブロックすると RailPosition 込みのコピーアイテムを得る
     * (レールコピー&ペースト機能)。
     */
    @Override
    public net.minecraft.world.item.ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityLargeRailBase) {
            TileEntityLargeRailCore coreTile = ((TileEntityLargeRailBase) tileEntity).getRailCore();
            if (coreTile != null) {
                return jp.ngt.rtm.item.ItemRail.copyItemFromRail(coreTile);
            }
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof TileEntityLargeRailBase tile0) {
                TileEntityLargeRailCore core = tile0.getRailCore();
                if (!world.isClientSide) {
                    if (core != null && !core.breaking) {
                        core.breaking = true;
                        RailMap[] maps = core.getAllRailMaps();
                        if (maps != null) {
                            Arrays.stream(maps).filter(java.util.Objects::nonNull)
                                    .forEach(rm -> rm.breakRail(world, core.getProperty(), core));
                        } else {
                            //RailMap 不明でもコアは撤去する
                            world.removeBlock(core.getBlockPos(), false);
                        }
                    } else if (core == null) {
                        jp.ngt.ngtlib.io.NGTLog.debug("[Rail] break at %s: core not found (startPoint=%d,%d,%d)",
                                pos.toShortString(), tile0.getStartPoint()[0], tile0.getStartPoint()[1], tile0.getStartPoint()[2]);
                    }
                }
            } else if (!world.isClientSide) {
                jp.ngt.ngtlib.io.NGTLog.debug("[Rail] break at %s: no rail BE (%s)", pos.toShortString(), String.valueOf(be));
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && this.isCore()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof TileEntityLargeRailCore core) {
                    core.tick();
                }
            };
        }
        return null;
    }
}
