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
     * 本家 NGTUtil.setValueToField : リフレクションでフィールドへ書込
     * (NGTO Builder が使用。SRG/現行名どちらでも探す)
     */
    public static void setValueToField(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) {
            return;
        }
        Class<?> cls = target.getClass();
        while (cls != null) {
            try {
                java.lang.reflect.Field f = cls.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            } catch (Exception e) {
                return;
            }
        }
    }

    /**
     * 本家 NGTUtil.getValueFromField 相当
     */
    public static Object getValueFromField(Object target, String fieldName) {
        if (target == null || fieldName == null) {
            return null;
        }
        Class<?> cls = target.getClass();
        while (cls != null) {
            try {
                java.lang.reflect.Field f = cls.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
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
