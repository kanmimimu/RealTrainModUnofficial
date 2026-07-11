package jp.ngt.mcte;

import net.minecraft.world.item.Item;

/**
 * MCTE (Minecraft Train Editor) のスクリプト互換。
 * NGTO Builder が MCTE.itemMiniature を参照する (無視リスト用)。
 */
@SuppressWarnings("unused")
public final class MCTE {
    private MCTE() {
    }

    public static Item itemMiniature;

    static {
        try {
            itemMiniature = com.portofino.realtrainmodunofficial.RealTrainModUnofficialItems.MINIATURE_ITEM.get();
        } catch (Throwable t) {
            jp.ngt.ngtlib.io.NGTLog.debug("[MCTE] item statics not ready: " + t);
        }
    }
}
