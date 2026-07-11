package jp.ngt.ngtlib.util;

import net.neoforged.fml.util.thread.EffectiveSide;

/**
 * 本家 NGTLib jp.ngt.ngtlib.util.NGTUtil の段階的移植。
 * Phase 0 時点では ObjectPool 等が必要とする最小限のみ。
 * Phase 3 (スクリプト API) で本家のメソッド群 (getSide, getMinecraft 系等) を拡充する。
 */
public final class NGTUtil {

    private NGTUtil() {
    }

    /**
     * 実効サイドがサーバーか (本家: FMLCommonHandler の effective side 判定)
     */
    public static boolean isServer() {
        return EffectiveSide.get().isServer();
    }

    public static boolean isClient() {
        return EffectiveSide.get().isClient();
    }

    /**
     * 本家: システム時間を使用
     */
    public static long getUniqueId() {
        return System.currentTimeMillis();
    }

    /**
     * 本家: クライアントプレイヤー (Client Only, スクリプト用)
     */
    public static net.minecraft.world.entity.player.Player getClientPlayer() {
        return net.minecraft.client.Minecraft.getInstance().player;
    }

    /**
     * 本家: クライアントワールド (Client Only, スクリプト用)
     */
    public static Object getClientWorld() {
        net.minecraft.world.level.Level level = net.minecraft.client.Minecraft.getInstance().level;
        return level != null ? new jp.ngt.mccompat.WorldCompat(level) : null;
    }

    /**
     * 本家 NGTUtil.reverse : 配列を逆順に
     */
    public static <T> void reverse(T[] array) {
        for (int i = 0, j = array.length - 1; i < j; ++i, --j) {
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * 本家 NGTUtil.addArray : 配列の全要素を List に追加
     */
    public static <T> void addArray(java.util.List<T> list, T[] array) {
        java.util.Collections.addAll(list, array);
    }
}
