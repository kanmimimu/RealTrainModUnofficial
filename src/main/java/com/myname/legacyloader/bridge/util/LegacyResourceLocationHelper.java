package com.myname.legacyloader.bridge.util;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public final class LegacyResourceLocationHelper {
    private LegacyResourceLocationHelper() {
    }

    public static ResourceLocation create(String location) {
        if (location == null || location.isEmpty()) {
            return ResourceLocation.withDefaultNamespace("missing");
        }
        int separator = location.indexOf(':');
        if (separator >= 0) {
            return create(location.substring(0, separator), location.substring(separator + 1));
        }
        return create("minecraft", location);
    }

    public static ResourceLocation create(String namespace, String path) {
        String safeNamespace = sanitizeNamespace(namespace);
        String safePath = sanitizePath(path);
        return ResourceLocation.fromNamespaceAndPath(safeNamespace, safePath);
    }

    private static String sanitizeNamespace(String namespace) {
        String value = namespace == null || namespace.isEmpty() ? "minecraft" : namespace;
        value = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        return value.isEmpty() ? "minecraft" : value;
    }

    private static String sanitizePath(String path) {
        String value = path == null || path.isEmpty() ? "missing" : path;
        value = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        return value.isEmpty() ? "missing" : value;
    }
}
