package com.myname.legacyloader.bridge.command;

import java.util.Collections;
import java.util.List;

public final class LegacyPlayerSelector {
    private LegacyPlayerSelector() {
    }

    public static Object matchOnePlayer(Object sender, String token) {
        return null;
    }

    public static List<?> matchPlayers(Object sender, String token) {
        return Collections.emptyList();
    }

    public static Object[] matchEntities(Object sender, String token, Class<?> entityClass) {
        return new Object[0];
    }

    public static boolean hasArguments(String token) {
        return token != null && token.startsWith("@");
    }

    public static boolean matchesMultiplePlayers(String token) {
        return token != null && ("@a".equals(token) || "@e".equals(token));
    }
}
