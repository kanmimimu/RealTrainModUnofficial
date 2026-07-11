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
     * getEntityByID
     */
    public net.minecraft.world.entity.Entity func_73045_a(int id) {
        return this.level.getEntity(id);
    }

    public net.minecraft.world.entity.Entity getEntityByID(int id) {
        return this.level.getEntity(id);
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
