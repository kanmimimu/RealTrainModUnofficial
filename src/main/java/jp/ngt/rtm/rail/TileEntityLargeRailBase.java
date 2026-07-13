package jp.ngt.rtm.rail;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 本家 jp.ngt.rtm.rail.TileEntityLargeRailBase (KaizPatchX) の忠実移植。
 * 道床(レールベース)ブロック1個ごとの BlockEntity。startPoint でコアを参照する。
 * 1.21 適合: TileEntityCustom/Lockable(MCTE) 依存は削除。NBT キー (spX/spY/spZ) は本家のまま。
 */
public class TileEntityLargeRailBase extends BlockEntity implements ILargeRail {
    private static final int SPLIT = 128;
    protected int[] startPoint = new int[3];

    /**
     * ブロックの当たり判定が設定されている
     */
    private boolean finishSetupBlockBounds;

    /**
     * {xNzP, xPzP, xPzN, xNzN}<br>
     * 描画と当たり判定の計算に使用
     */
    private float[] blockHeights;

    /**
     * 当たり判定をブロック内で何分割するか (N×N のマス目)。
     * <p>
     * Minecraft の当たり判定 (VoxelShape) は軸平行な箱しか作れないので、斜面そのものは
     * 表現できない。1ブロック=1つの平らな箱にすると坂が「1ブロックごとの段差」になるため、
     * ブロック内を細かく割って階段状に近似する。N=4 なら段差は勾配の 1/4 になり、
     * プレイヤーの自動ステップ(0.6)より十分小さくなるので実質スロープとして歩ける。
     */
    private static final int SHAPE_SPLIT = 4;

    /**
     * 当たり判定の形状キャッシュ。blockHeights を作り直したら捨てる。
     * 当たり判定は毎tick何度も引かれるので、毎回 N×N 個の箱を union すると重い。
     */
    private net.minecraft.world.phys.shapes.VoxelShape cachedShape;

    public TileEntityLargeRailBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileEntityLargeRailBase(BlockPos pos, BlockState state) {
        this(RTMRailBlockEntities.LARGE_RAIL_BASE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);

        this.startPoint[0] = nbt.getInt("spX");
        this.startPoint[1] = nbt.getInt("spY");
        this.startPoint[2] = nbt.getInt("spZ");
        //同期/再読込時に高さキャッシュを無効化 (当たり判定が古い値に固着しないように)
        this.blockHeights = null;
        this.cachedShape = null;
        this.finishSetupBlockBounds = false;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);

        nbt.putInt("spX", this.startPoint[0]);
        nbt.putInt("spY", this.startPoint[1]);
        nbt.putInt("spZ", this.startPoint[2]);
    }

    // クライアント同期 (本家 S35 相当): フル NBT を送る
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int[] getStartPoint() {
        return this.startPoint;
    }

    public void setStartPoint(int x, int y, int z) {
        this.startPoint[0] = x;
        this.startPoint[1] = y;
        this.startPoint[2] = z;
    }

    public boolean isTrainOnRail() {
        TileEntityLargeRailCore tile = this.getRailCore();
        if (tile != null) {
            return tile.isCollidedTrain();
        }
        return false;
    }

    /**
     * スクリプト互換 SRG: func_70296_d = markDirty (setChanged)。
     * SRB3 のサーバースクリプトがレール生成後に呼ぶ。
     */
    public void func_70296_d() {
        this.setChanged();
    }

    @Override
    public RailMap getRailMap(Entity entity) {
        TileEntityLargeRailCore tile = this.getRailCore();
        if (tile != null) {
            return ((ILargeRail) tile).getRailMap(entity);
        }
        return null;
    }

    public TileEntityLargeRailCore getRailCore() {
        if (this.level == null) {
            return null;
        }
        BlockEntity tile = this.level.getBlockEntity(new BlockPos(this.startPoint[0], this.startPoint[1], this.startPoint[2]));
        if (tile instanceof TileEntityLargeRailCore) {
            return (TileEntityLargeRailCore) tile;
        }
        return null;
    }

    public static TileEntityLargeRailBase getRailFromCoordinates(Level world, double px, double py, double pz) {
        int x = Mth.floor(px);
        int y = Mth.floor(py);
        int z = Mth.floor(pz);
        while (y > world.getMinBuildHeight()) {
            Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
            if (block instanceof BlockLargeRailBase) {
                break;
            }
            --y;
        }

        BlockEntity tile = world.getBlockEntity(new BlockPos(x, y, z));
        if (tile instanceof TileEntityLargeRailBase) {
            return (TileEntityLargeRailBase) tile;
        }
        return null;
    }

    /**
     * 指定した座標の下にあるレールのRailMapを返す
     *
     * @param entity 列車、車止め、など
     */
    public static RailMap getRailMapFromCoordinates(Level world, Entity entity, double px, double py, double pz) {
        TileEntityLargeRailBase rail = TileEntityLargeRailBase.getRailFromCoordinates(world, px, py, pz);
        if (rail != null) {
            return rail.getRailMap(entity);
        }
        return null;
    }

    /**
     * 坂に沿った当たり判定。
     * <p>
     * 本家は「1ブロック = 4隅の平均高さの平らな箱」1つなので、坂だとブロックごとの段差になる。
     * ここではブロック内を {@link #SHAPE_SPLIT}×{@link #SHAPE_SPLIT} のマス目に割り、4隅の高さを
     * 双線形補間してマスごとに箱を積むことで、階段の刻みを勾配の 1/N まで細かくしている
     * (VoxelShape は軸平行な箱しか作れないので、斜面そのものは表現できない)。
     * <p>
     * マス内は「4隅の最大」を採るので、当たり判定がレール面より下がることはない。
     *
     * @param thickness 最低の厚み (本家 BlockLargeRailBase.THICKNESS = 1/16)
     */
    public net.minecraft.world.phys.shapes.VoxelShape getRailCollisionShape(int x, int y, int z, float thickness) {
        net.minecraft.world.phys.shapes.VoxelShape shape = this.cachedShape;
        if (shape != null) {
            return shape;
        }

        float[] fa = this.getBlockHeights(x, y, z, thickness, true);
        shape = net.minecraft.world.phys.shapes.Shapes.empty();
        for (int i = 0; i < SHAPE_SPLIT; i++) {
            for (int j = 0; j < SHAPE_SPLIT; j++) {
                double u0 = (double) i / SHAPE_SPLIT;
                double u1 = (double) (i + 1) / SHAPE_SPLIT;
                double v0 = (double) j / SHAPE_SPLIT;
                double v1 = (double) (j + 1) / SHAPE_SPLIT;
                double h = Math.max(
                        Math.max(interpolateHeight(fa, u0, v0), interpolateHeight(fa, u1, v0)),
                        Math.max(interpolateHeight(fa, u0, v1), interpolateHeight(fa, u1, v1)));
                if (h < thickness) {
                    h = thickness;
                }
                shape = net.minecraft.world.phys.shapes.Shapes.or(shape,
                        net.minecraft.world.phys.shapes.Shapes.box(u0, 0.0D, v0, u1, h, v1));
            }
        }
        shape = shape.optimize();
        this.cachedShape = shape;
        return shape;
    }

    /**
     * 4隅の高さ {xNzP, xPzP, xPzN, xNzN} を双線形補間する。
     * u: 0=xN, 1=xP / v: 0=zN, 1=zP
     */
    private static double interpolateHeight(float[] fa, double u, double v) {
        double zn = fa[3] + (fa[2] - fa[3]) * u;//zN 側の辺: xN → xP
        double zp = fa[0] + (fa[1] - fa[0]) * u;//zP 側の辺: xN → xP
        return zn + (zp - zn) * v;
    }

    /**
     * {xNzP, xPzP, xPzN, xNzN}
     */
    public float[] getBlockHeights(int x, int y, int z, float defaultHeight, boolean useCache) {
        if (useCache && this.blockHeights != null) {
            return this.blockHeights;
        }

        if (this.finishSetupBlockBounds || !useCache) {
            float[] fa = this.getBlockHeights(x, y, z, defaultHeight);
            if (fa != null) {
                if (useCache) {
                    this.blockHeights = fa;
                    this.cachedShape = null;
                    if (this.level != null && !this.level.isClientSide) {
                        this.finishSetupBlockBounds = true;
                    }
                }
                return fa;
            }
        } else {
            // 初回: コアが解決できれば計算を有効化 (本家 updateEntity の finishSetupBlockBounds 相当)
            if (this.getRailCore() != null) {
                this.finishSetupBlockBounds = true;
                float[] fa = this.getBlockHeights(x, y, z, defaultHeight);
                if (fa != null) {
                    if (useCache) {
                        this.blockHeights = fa;
                        this.cachedShape = null;
                    }
                    return fa;
                }
            }
        }

        return new float[]{0.0625F, 0.0625F, 0.0625F, 0.0625F};
    }

    private float[] getBlockHeights(int x, int y, int z, float defaultHeight) {
        TileEntityLargeRailCore core = this.getRailCore();
        if (core == null) {
            return null;
        }

        RailMap[] rms = core.getAllRailMaps();
        if (rms == null) {
            return null;
        }

        float[] fa = new float[]{defaultHeight, defaultHeight, defaultHeight, defaultHeight};
        for (int i = 0; i < fa.length; ++i) {
            int x0 = x + ((i == 1 || i == 2) ? 1 : 0);
            int z0 = z + ((i == 0 || i == 1) ? 1 : 0);
            double distanceSq = Double.MAX_VALUE;

            for (RailMap rm : rms) {
                if (rm == null) {
                    return null;
                }
                int index = rm.getNearlestPoint(SPLIT, x0, z0);
                if (index < 0) {
                    index = 0;
                }

                double[] rpos = rm.getRailPos(SPLIT, index);
                double dSq2 = NGTMath.getDistanceSq(x0, z0, rpos[1], rpos[0]);
                if (dSq2 < distanceSq) {
                    distanceSq = dSq2;

                    double height = rm.getRailHeight(SPLIT, index);
                    float yaw = rm.getRailRotation(SPLIT, index);
                    float cant = rm.getCant(SPLIT, index);
                    float yaw2 = (float) NGTMath.toDegrees(Math.atan2(rpos[1] - x0, rpos[0] - z0));
                    //最も近いレール上の点からの距離
                    double len = Math.sqrt((rpos[1] - x0) * (rpos[1] - x0) + (rpos[0] - z0) * (rpos[0] - z0));
                    //レールYawに対するベクトル角により左右位置を判断
                    boolean dirFlag = Mth.wrapDegrees(yaw2 - yaw) > 0.0F;
                    double h2 = NGTMath.sin(cant) * len * (dirFlag ? -1.0F : 1.0F);
                    fa[i] = (float) (height - (double) y + h2 + defaultHeight - 0.0625);
                }
            }
        }
        return fa;
    }

    /**
     * 音が響くレールかどうか
     */
    public boolean isReberbSound() {
        TileEntityLargeRailCore core = this.getRailCore();
        if (core != null && this.level != null) {
            if (!core.getProperty().block.defaultBlockState().isSolidRender(this.level, this.worldPosition)) {
                BlockState state = this.level.getBlockState(this.worldPosition.below());
                return !state.isSolidRender(this.level, this.worldPosition.below());
            }
        }
        return false;
    }
}
