package com.myname.legacyloader.bridge.client;

import net.minecraft.resources.ResourceLocation;

public class LegacyTextureMap implements LegacyIconRegister {
    public static final ResourceLocation field_110575_b = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    public static final ResourceLocation field_110576_c = ResourceLocation.withDefaultNamespace("textures/atlas/items.png");

    private final int textureType;

    public LegacyTextureMap() {
        this(0);
    }

    public LegacyTextureMap(int textureType) {
        this.textureType = textureType;
    }

    public int func_130086_a() {
        return textureType;
    }

    @Override
    public LegacyIcon registerIcon(String name) {
        return new SimpleIcon(normalizeIconName(name));
    }

    public LegacyIcon func_110572_b(String name) {
        return new SimpleIcon(normalizeIconName(name));
    }

    private static String normalizeIconName(String name) {
        if (name == null || name.isBlank()) return "missingno";
        String out = name.replace('\\', '/');
        if (out.startsWith("/")) out = out.substring(1);
        if (out.endsWith(".png")) out = out.substring(0, out.length() - 4);
        if (out.startsWith("textures/")) out = out.substring("textures/".length());
        if (out.startsWith("blocks/")) out = out.substring("blocks/".length());
        if (out.startsWith("items/")) out = out.substring("items/".length());
        return out.toLowerCase(java.util.Locale.ROOT);
    }

    private static final class SimpleIcon implements LegacyIcon {
        private final String name;

        private SimpleIcon(String name) {
            this.name = name == null ? "missingno" : name;
        }

        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        @Override public float getMinU() { return 0; }
        @Override public float getMaxU() { return 1; }
        @Override public float getMinV() { return 0; }
        @Override public float getMaxV() { return 1; }
        @Override public String getIconName() { return name; }
    }
}
