package com.myname.legacyloader.bridge.client.config;

public final class LegacyI18n {
    private LegacyI18n() {
    }

    public static String func_135052_a(String key, Object... args) {
        return key;
    }

    public static String format(String key, Object... args) {
        return func_135052_a(key, args);
    }
}
