package com.myname.legacyloader.bridge.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LegacyIntHashMap {
    private final Map<Integer, Object> values = new HashMap<>();

    public Object lookup(int key) {
        return values.get(key);
    }

    public Object func_76041_a(int key) {
        return lookup(key);
    }

    public boolean containsItem(int key) {
        return values.containsKey(key);
    }

    public boolean func_76037_b(int key) {
        return containsItem(key);
    }

    public void addKey(int key, Object value) {
        values.put(key, value);
    }

    public void func_76038_a(int key, Object value) {
        addKey(key, value);
    }

    public Object removeObject(int key) {
        return values.remove(key);
    }

    public Object func_76049_d(int key) {
        return removeObject(key);
    }

    public void clearMap() {
        values.clear();
    }

    public void func_76046_c() {
        clearMap();
    }

    public Set<Integer> getKeySet() {
        return values.keySet();
    }
}
