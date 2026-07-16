package jp.ngt.rtm.modelpack.state;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本家 jp.ngt.rtm.modelpack.state.DataMap のスクリプト互換移植。
 * set 系の第3引数は本家では同期フラグ (0:なし, 1:server→client)。
 *
 * <p>flag=1 の書き込みは {@link #drainPendingSync()} に溜まり、EntityTrainBase が
 * 定期的にクライアントへ配信する (DataMapSyncPayload)。ATSA の HUD や WebCTC が
 * サーバー側で書いた値をクライアントで読めるのはこの仕組みによる。
 */
public class DataMap {
    private final Map<String, Object> map = new ConcurrentHashMap<>();
    /** flag=1 で書かれた同期待ちエントリ (サーバー側でのみ溜まる)。 */
    private final Map<String, Object> pendingSync = new ConcurrentHashMap<>();

    public int getInt(String key) {
        Object v = this.map.get(key);
        return v instanceof Number n ? n.intValue() : 0;
    }

    public void setInt(String key, int value, int flag) {
        this.map.put(key, value);
        if (flag == 1) {
            this.pendingSync.put(key, value);
        }
    }

    public boolean getBoolean(String key) {
        Object v = this.map.get(key);
        return v instanceof Boolean b && b;
    }

    public void setBoolean(String key, boolean value, int flag) {
        this.map.put(key, value);
        if (flag == 1) {
            this.pendingSync.put(key, value);
        }
    }

    public double getDouble(String key) {
        Object v = this.map.get(key);
        return v instanceof Number n ? n.doubleValue() : 0.0D;
    }

    public void setDouble(String key, double value, int flag) {
        this.map.put(key, value);
        if (flag == 1) {
            this.pendingSync.put(key, value);
        }
    }

    public String getString(String key) {
        Object v = this.map.get(key);
        return v instanceof String s ? s : "";
    }

    public void setString(String key, String value, int flag) {
        this.map.put(key, value == null ? "" : value);
        if (flag == 1) {
            this.pendingSync.put(key, value == null ? "" : value);
        }
    }

    public boolean contains(String key) {
        return this.map.containsKey(key);
    }

    /** 全エントリのコピー (DataMapEditor 等の閲覧用)。 */
    public Map<String, Object> getEntries() {
        return new HashMap<>(this.map);
    }

    /** 同期待ちエントリを取り出してクリアする (サーバーの配信処理用)。空なら空マップ。 */
    public Map<String, Object> drainPendingSync() {
        if (this.pendingSync.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> out = new HashMap<>(this.pendingSync);
        this.pendingSync.clear();
        return out;
    }

    /** 同期パケットの適用 (クライアント側)。型プレフィクス付き文字列から復元する。 */
    public void applySyncedValue(String key, String encoded) {
        if (encoded == null || encoded.length() < 2) {
            return;
        }
        char type = encoded.charAt(0);
        String body = encoded.substring(2);
        try {
            switch (type) {
                case 'I' -> this.map.put(key, Integer.parseInt(body));
                case 'D' -> this.map.put(key, Double.parseDouble(body));
                case 'B' -> this.map.put(key, Boolean.parseBoolean(body));
                default -> this.map.put(key, body);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    /** 同期パケット用に値を型プレフィクス付き文字列へ ("I:5" / "D:1.5" / "B:true" / "S:xxx")。 */
    public static String encodeSyncedValue(Object value) {
        if (value instanceof Integer i) {
            return "I:" + i;
        }
        if (value instanceof Double d) {
            return "D:" + d;
        }
        if (value instanceof Boolean b) {
            return "B:" + b;
        }
        return "S:" + value;
    }
}
