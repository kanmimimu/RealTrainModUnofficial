package jp.ngt.rtm.modelpack.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本家 jp.ngt.rtm.modelpack.state.DataMap のスクリプト互換移植。
 * set 系の第3引数は本家では同期フラグ (0:なし, 1:server→client, ...)。
 * TODO: ネットワーク同期 (現状はローカル保持のみ)。
 */
public class DataMap {
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    public int getInt(String key) {
        Object v = this.map.get(key);
        return v instanceof Number n ? n.intValue() : 0;
    }

    public void setInt(String key, int value, int flag) {
        this.map.put(key, value);
    }

    public boolean getBoolean(String key) {
        Object v = this.map.get(key);
        return v instanceof Boolean b && b;
    }

    public void setBoolean(String key, boolean value, int flag) {
        this.map.put(key, value);
    }

    public double getDouble(String key) {
        Object v = this.map.get(key);
        return v instanceof Number n ? n.doubleValue() : 0.0D;
    }

    public void setDouble(String key, double value, int flag) {
        this.map.put(key, value);
    }

    public String getString(String key) {
        Object v = this.map.get(key);
        return v instanceof String s ? s : "";
    }

    public void setString(String key, String value, int flag) {
        this.map.put(key, value == null ? "" : value);
    }

    public boolean contains(String key) {
        return this.map.containsKey(key);
    }
}
