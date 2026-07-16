package com.myname.legacyloader.bridge.fml;

import net.minecraft.client.Minecraft;

public class LegacyFMLClientHandler {
    private static final LegacyFMLClientHandler INSTANCE = new LegacyFMLClientHandler();

    public static LegacyFMLClientHandler instance() {
        return INSTANCE;
    }

    public Minecraft getClient() {
        return Minecraft.getInstance();
    }

    // SRG蜷搾ｼ磯屮隱ｭ蛹冶ｧ｣髯､蜷搾ｼ峨お繧､繝ｪ繧｢繧ｹ
    public Minecraft func_71410_x() {
        return getClient();
    }
}