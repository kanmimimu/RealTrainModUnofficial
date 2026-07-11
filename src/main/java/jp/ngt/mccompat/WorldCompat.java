package jp.ngt.mccompat;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * パックスクリプト互換: entity.field_70170_p (1.7.10 の World) のラッパ。
 * スクリプトが呼ぶ SRG メソッド/フィールドを 1.21 Level に委譲する。
 */
@SuppressWarnings("unused")
public class WorldCompat {
    public final Level level;
    /**
     * 1.7.10 SRG: isRemote
     */
    public final boolean field_72995_K;

    public WorldCompat(Level level) {
        this.level = level;
        this.field_72995_K = level.isClientSide;
    }

    public Level getLevel() {
        return this.level;
    }

    /**
     * getCelestialAngle
     */
    public float func_72929_e(float partialTick) {
        return this.level.getTimeOfDay(partialTick);
    }

    public float getCelestialAngle(float partialTick) {
        return this.func_72929_e(partialTick);
    }

    /**
     * 1.12 SRG: getLightFor(EnumSkyBlock, BlockPos)
     */
    public int func_175642_b(Object skyBlock, Object pos) {
        return this.getLight(skyBlock, pos);
    }

    /**
     * 1.7.10 SRG: getSavedLightValue(EnumSkyBlock, x, y, z)
     */
    public int func_72972_b(Object skyBlock, int x, int y, int z) {
        return this.getLight(skyBlock, new BlockPos(x, y, z));
    }

    private int getLight(Object skyBlock, Object pos) {
        net.minecraft.world.level.LightLayer layer = skyBlock instanceof EnumSkyBlock esb
                ? esb.layer : net.minecraft.world.level.LightLayer.BLOCK;
        BlockPos bp = pos instanceof BlockPos b ? b : BlockPos.ZERO;
        return this.level.getBrightness(layer, bp);
    }

    /**
     * getWorldTime
     */
    public long func_72820_D() {
        return this.level.getDayTime();
    }

    public long getWorldTime() {
        return this.level.getDayTime();
    }

    public long getTotalWorldTime() {
        return this.level.getGameTime();
    }

    /**
     * getEntityByID。プレイヤーは PlayerCompat ラッパーで返す
     * (SRB3 等が MCWrapperClient.getPlayer() の戻り値と === 比較するため)。
     */
    public Object func_73045_a(int id) {
        return wrapEntity(this.level.getEntity(id));
    }

    /** 文字列 ID も受ける (dataMap.getString の値をそのまま渡すスクリプト用) */
    public Object func_73045_a(Object id) {
        if (id == null) {
            return null;
        }
        try {
            int i = id instanceof Number n ? n.intValue() : Integer.parseInt(id.toString().trim());
            return wrapEntity(this.level.getEntity(i));
        } catch (Exception e) {
            return null;
        }
    }

    public net.minecraft.world.entity.Entity getEntityByID(int id) {
        return this.level.getEntity(id);
    }

    private static Object wrapEntity(net.minecraft.world.entity.Entity e) {
        if (e instanceof net.minecraft.world.entity.player.Player p) {
            return PlayerCompat.of(p);
        }
        return e;
    }

    // ===== 1.7.10 ブロック操作 SRG (SRB3/NGTO Builder のサーバースクリプトが使用) =====

    /** func_147465_d = setBlock(x,y,z, Block, meta, flag) */
    public boolean func_147465_d(double x, double y, double z, Object block, int meta, int flag) {
        net.minecraft.world.level.block.Block b = asBlock(block);
        if (b == null) {
            return false;
        }
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        return this.level.setBlock(pos, b.defaultBlockState(), flag);
    }

    /** func_147468_f = setBlockToAir */
    public boolean func_147468_f(double x, double y, double z) {
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        return this.level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
    }

    /** func_147438_o = getTileEntity(x,y,z) */
    public net.minecraft.world.level.block.entity.BlockEntity func_147438_o(double x, double y, double z) {
        return this.level.getBlockEntity(new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z)));
    }

    /** func_175625_s = getTileEntity(BlockPos) (1.12) */
    public net.minecraft.world.level.block.entity.BlockEntity func_175625_s(BlockPos pos) {
        return pos != null ? this.level.getBlockEntity(pos) : null;
    }

    /** func_147439_a = getBlock(x,y,z) */
    public net.minecraft.world.level.block.Block func_147439_a(double x, double y, double z) {
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        return this.level.getBlockState(pos).getBlock();
    }

    /** func_72805_g = getBlockMetadata (1.21 にメタは無い) */
    public int func_72805_g(double x, double y, double z) {
        return 0;
    }

    /** func_147471_g = markBlockForUpdate */
    public void func_147471_g(double x, double y, double z) {
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        net.minecraft.world.level.block.state.BlockState st = this.level.getBlockState(pos);
        this.level.sendBlockUpdated(pos, st, st, 3);
    }

    /** func_180495_p = getBlockState (1.12) */
    public net.minecraft.world.level.block.state.BlockState func_180495_p(BlockPos pos) {
        return pos != null ? this.level.getBlockState(pos) : null;
    }

    private static net.minecraft.world.level.block.Block asBlock(Object block) {
        if (block instanceof net.minecraft.world.level.block.Block b) {
            return b;
        }
        return null;
    }

    /**
     * isRemote 相当のアクセサ
     */
    public boolean isRemote() {
        return this.level.isClientSide;
    }

    /**
     * rand 相当
     */
    public final java.util.Random rand = new java.util.Random();
}
