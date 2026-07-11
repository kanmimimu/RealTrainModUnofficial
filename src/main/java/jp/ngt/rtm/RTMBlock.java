package jp.ngt.rtm;

import net.minecraft.world.level.block.Block;

/**
 * 本家 jp.ngt.rtm.RTMBlock のスクリプト互換 (マーカー参照等)。
 */
@SuppressWarnings("unused")
public final class RTMBlock {
    private RTMBlock() {
    }

    public static Block marker;
    public static Block markerSwitch;

    static {
        try {
            marker = jp.ngt.rtm.rail.RTMRailBlocks.MARKER.get();
            markerSwitch = jp.ngt.rtm.rail.RTMRailBlocks.MARKER_SWITCH.get();
        } catch (Throwable t) {
            jp.ngt.ngtlib.io.NGTLog.debug("[RTMBlock] block statics not ready: " + t);
        }
    }
}
