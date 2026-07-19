package com.portofino.realtrainmodunofficial;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RTMU クライアント設定 (ポーズメニューの「RTMU設定」から変更)。軽量化欄:
 * <ul>
 *   <li>vehicleRenderDistance: 車両描画距離 (0=無制限)</li>
 *   <li>staticVehicleThrottle: 静止車両の再計算頻度 (0=標準/1=省エネ/2=積極)</li>
 *   <li>skipDistantVehicleExtras: 遠方車両のライト・方向幕を省略するか</li>
 * </ul>
 * クライアントの値はファイルへ永続化し、サーバーへは {@code RtmuSettingsPayload} で同期する。
 */
public final class RtmuSettings {

    // ---- 軽量化 (すべて opt-in。既定値は「見た目が変わらない」側) ----
    /**
     * 車両描画距離 (ブロック)。<b>0 = 無制限 (バニラどおり)</b>。>0 のとき、この距離より遠い
     * 車両は<b>丸ごと描画をスキップ</b>する。RTMU の最大負荷は「スクリプト車両の毎フレーム
     * Nashorn/GraalJS 実行」なので、遠方車両を間引くのが最も効く。既定 0。
     */
    public static int vehicleRenderDistance = 0;
    /**
     * 静止車両の再計算頻度。停車してドア/パンタも動いていない車両は描画結果をキャッシュして
     * 毎フレームのスクリプト実行を省くが、点滅灯・スクロール表示等の時間依存アニメのために
     * 一定間隔で描き直す。その間隔を選ぶ:
     * <ul>
     *   <li>0 = 標準 (約6Hz。点滅も滑らか。既定)</li>
     *   <li>1 = 省エネ (約2Hz)</li>
     *   <li>2 = 積極 (約1Hz。停車中の点滅がカクつくが最も軽い)</li>
     * </ul>
     */
    public static int staticVehicleThrottle = 0;
    /**
     * 遠方車両のライト・方向幕を省略する。ON のとき、車両描画距離の半分より遠い車両は
     * 前照灯/尾灯/室内灯の発光パスと方向幕/種別幕オーバーレイ (どちらも追加のスクリプト実行) を
     * 省く。車体は出る。車両描画距離が無制限のときは 64m を基準にする。既定 OFF。
     */
    public static boolean skipDistantVehicleExtras = false;

    // ---- サーバー側: プレイヤー別に同期された設定 (現状これを読む常駐ロジックは無いが、
    //      移植元と構成を揃えるため同期経路自体は用意しておく) ----
    private static final Map<UUID, Integer> SERVER_VEHICLE_RENDER_DISTANCE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SERVER_STATIC_VEHICLE_THROTTLE = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> SERVER_SKIP_DISTANT_EXTRAS = new ConcurrentHashMap<>();

    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("realtrainmodunofficial").resolve("rtmu_settings.properties");

    private RtmuSettings() {
    }

    /** 車両描画距離。0 = 無制限、それ以外は 32〜256 に丸める。 */
    public static int clampVehicleRenderDistance(int d) {
        if (d <= 0) {
            return 0;
        }
        return Math.max(32, Math.min(256, d));
    }

    /**
     * 車両描画距離を超えているか (カメラ座標との距離で判定)。
     * 0 = 無制限なら常に false。全車両レンダラーの shouldRender から呼ぶ。
     */
    public static boolean beyondVehicleRenderDistance(double ex, double ey, double ez,
                                                      double camX, double camY, double camZ) {
        int limit = vehicleRenderDistance;
        if (limit <= 0) {
            return false;
        }
        double dx = ex - camX;
        double dy = ey - camY;
        double dz = ez - camZ;
        return dx * dx + dy * dy + dz * dz > (double) limit * (double) limit;
    }

    /** 静止車両の再計算間隔 (フレーム数)。throttle 0/1/2 → 10/30/60。 */
    public static int staticVehicleRefreshFrames() {
        return switch (staticVehicleThrottle) {
            case 1 -> 30;
            case 2 -> 60;
            default -> 10;
        };
    }

    /**
     * 遠方車両の追加描画 (ライト/方向幕) を省くしきい値の二乗 (m^2)。0 = 省かない。
     * 車両描画距離が有効ならその半分、無制限なら 64m を基準にする。
     */
    public static double distantExtrasCutoffSq() {
        if (!skipDistantVehicleExtras) {
            return 0.0D;
        }
        double d = vehicleRenderDistance > 0 ? vehicleRenderDistance * 0.5D : 64.0D;
        return d * d;
    }

    // ===== クライアント: 永続化 =====

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                return;
            }
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(FILE)) {
                p.load(in);
            }
            vehicleRenderDistance = clampVehicleRenderDistance(parseInt(p.getProperty("vehicleRenderDistance", "0"), 0));
            staticVehicleThrottle = Math.max(0, Math.min(2, parseInt(p.getProperty("staticVehicleThrottle", "0"), 0)));
            skipDistantVehicleExtras = Boolean.parseBoolean(p.getProperty("skipDistantVehicleExtras", "false"));
        } catch (Exception e) {
            RealTrainModUnofficial.LOGGER.warn("RTMU: failed to load settings", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Properties p = new Properties();
            p.setProperty("vehicleRenderDistance", Integer.toString(vehicleRenderDistance));
            p.setProperty("staticVehicleThrottle", Integer.toString(staticVehicleThrottle));
            p.setProperty("skipDistantVehicleExtras", Boolean.toString(skipDistantVehicleExtras));
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "RTMU client settings");
            }
        } catch (IOException e) {
            RealTrainModUnofficial.LOGGER.warn("RTMU: failed to save settings", e);
        }
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    // ===== サーバー: プレイヤー別の同期値 =====

    public static void setServerValues(UUID player, int vehicleRenderDistance, int staticVehicleThrottle,
                                       boolean skipDistantVehicleExtras) {
        SERVER_VEHICLE_RENDER_DISTANCE.put(player, clampVehicleRenderDistance(vehicleRenderDistance));
        SERVER_STATIC_VEHICLE_THROTTLE.put(player, Math.max(0, Math.min(2, staticVehicleThrottle)));
        SERVER_SKIP_DISTANT_EXTRAS.put(player, skipDistantVehicleExtras);
    }
}
