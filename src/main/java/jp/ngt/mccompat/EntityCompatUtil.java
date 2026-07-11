package jp.ngt.mccompat;

import net.minecraft.world.entity.Entity;

/**
 * スクリプトから渡される「エンティティらしきもの」(実 Entity / PlayerCompat /
 * CarEntity 等) を実 Entity に解決する共通ヘルパー。
 */
public final class EntityCompatUtil {
    private EntityCompatUtil() {
    }

    public static Entity unwrapEntity(Object obj) {
        if (obj instanceof Entity e) {
            return e;
        }
        if (obj instanceof PlayerCompat p) {
            return p.player;
        }
        return null;
    }
}
