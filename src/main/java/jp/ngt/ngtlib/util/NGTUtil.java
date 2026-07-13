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
     * <p>
     * ★ このクラスから {@code net.minecraft.client.*} を<b>直接参照してはいけない</b>。
     * <p>
     * 以前ここは {@code Minecraft.getInstance().player} を直接返していた。フィールドの型は
     * {@code LocalPlayer} で戻り値は {@code Player} なので、JVM の<b>検証器が代入互換性を
     * 確かめるために LocalPlayer を読み込む</b>。専用サーバーではクライアントクラスが存在
     * しないため、{@code NGTUtil} は<b>クラスのロード時点で</b> NoClassDefFoundError になり、
     * このクラスのどのメソッドも呼べなくなる (連結処理の {@code NGTUtil.reverse} が巻き添えで
     * 落ち、編成が壊れて列車が消えていた)。
     * <p>
     * メソッド解決は遅延なので「呼ばなければ安全」ではない。<b>戻り値やフィールドの型として
     * 現れるだけで検証時にロードされる</b>。そのためクライアント専用の処理は
     * {@link NGTUtilClient} 側に置き、こちらからは dist を見てリフレクションで呼ぶ。
     */
    public static net.minecraft.world.entity.player.Player getClientPlayer() {
        Object player = invokeClientOnly("getClientPlayer");
        return player instanceof net.minecraft.world.entity.player.Player p ? p : null;
    }

    /**
     * 本家: クライアントワールド (Client Only, スクリプト用)
     */
    public static Object getClientWorld() {
        return invokeClientOnly("getClientWorld");
    }

    /**
     * クライアント専用処理を {@link NGTUtilClient} に投げる。専用サーバーでは何もしない。
     * <p>
     * リフレクションなのは、このクラスの<b>バイトコードにクライアント型を一切登場させない</b>
     * ため。クラス名を文字列で持てば、専用サーバーでは NGTUtilClient がロードされない。
     */
    private static Object invokeClientOnly(String methodName) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist != net.neoforged.api.distmarker.Dist.CLIENT) {
            return null;
        }
        try {
            Class<?> clientUtil = Class.forName("jp.ngt.ngtlib.util.NGTUtilClient");
            return clientUtil.getMethod(methodName).invoke(null);
        } catch (Throwable t) {
            return null;
        }
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
