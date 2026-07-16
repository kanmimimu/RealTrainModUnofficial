package com.myname.legacyloader.bridge.util;

import java.util.HashMap;
import java.util.Map;

public class LegacyLongHashMap {
    private final Map<Long, Object> values = new HashMap<>();

    public int getNumHashElements() {
        return values.size();
    }

    public int func_76162_a() {
        return getNumHashElements();
    }

    public Object getValueByKey(long key) {
        return values.get(key);
    }

    public Object func_76164_a(long key) {
        return getValueByKey(key);
    }

    public boolean containsItem(long key) {
        return values.containsKey(key);
    }

    public boolean func_76161_b(long key) {
        return containsItem(key);
    }

    public void add(long key, Object value) {
        values.put(key, value);
    }

    public void func_76163_a(long key, Object value) {
        add(key, value);
    }

    public Object remove(long key) {
        return values.remove(key);
    }

    public Object func_76159_d(long key) {
        return remove(key);
    }
}
