package jp.ngt.rtm;

import net.minecraft.world.item.Item;

/**
 * 本家 jp.ngt.rtm.RTMItem のスクリプト互換。
 * SRB3 の getPlayerRail が item.func_77973_b() === RTMItem.itemLargeRail で
 * 手持ちレールを判定する。
 */
@SuppressWarnings("unused")
public final class RTMItem {
    private RTMItem() {
    }

    public static Item itemLargeRail;

    static {
        try {
            itemLargeRail = com.portofino.realtrainmodunofficial.RealTrainModUnofficialItems.RAIL_ITEM.get();
        } catch (Throwable t) {
            jp.ngt.ngtlib.io.NGTLog.debug("[RTMItem] item statics not ready: " + t);
        }
    }
}
