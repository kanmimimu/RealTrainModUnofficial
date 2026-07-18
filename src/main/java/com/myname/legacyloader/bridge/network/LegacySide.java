package com.myname.legacyloader.bridge.network;

/**
 * 1.7.10縺ｮ Side 莠呈鋤蛻玲嫌蝙・
 */
public enum LegacySide {
    CLIENT,
    SERVER;

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isServer() {
        return this == SERVER;
    }

    // 1.7.10縺ｮ螳壽焚蜷・
    public static final LegacySide field_1823a = CLIENT;
    public static final LegacySide field_1824b = SERVER;
}