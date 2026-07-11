package jp.ngt.ngtlib.util;

import jp.ngt.mccompat.EntityCompatUtil;
import net.minecraft.world.entity.Entity;

/**
 * 本家 jp.ngt.ngtlib.util.MCWrapper のスクリプト互換。
 * エンティティ引数は 実 Entity / PlayerCompat のどちらでも受ける。
 */
@SuppressWarnings("unused")
public final class MCWrapper {
    private MCWrapper() {
    }

    public static double getPosX(Object entity) {
        Entity e = EntityCompatUtil.unwrapEntity(entity);
        return e != null ? e.getX() : 0.0D;
    }

    public static double getPosY(Object entity) {
        Entity e = EntityCompatUtil.unwrapEntity(entity);
        return e != null ? e.getY() : 0.0D;
    }

    public static double getPosZ(Object entity) {
        Entity e = EntityCompatUtil.unwrapEntity(entity);
        return e != null ? e.getZ() : 0.0D;
    }

    public static double getYaw(Object entity) {
        Entity e = EntityCompatUtil.unwrapEntity(entity);
        return e != null ? e.getYRot() : 0.0D;
    }

    public static double getPitch(Object entity) {
        Entity e = EntityCompatUtil.unwrapEntity(entity);
        return e != null ? e.getXRot() : 0.0D;
    }
}
