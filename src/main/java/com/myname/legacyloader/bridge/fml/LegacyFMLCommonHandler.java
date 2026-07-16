package com.myname.legacyloader.bridge.fml;

import com.myname.legacyloader.bridge.network.LegacySide;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class LegacyFMLCommonHandler {
    private static final LegacyFMLCommonHandler INSTANCE = new LegacyFMLCommonHandler();

    private final BridgeEventBus bus = new BridgeEventBus();

    public static LegacyFMLCommonHandler instance() {
        return INSTANCE;
    }

    public BridgeEventBus bus() {
        return bus;
    }

    public BridgeEventBus func_71554_a() {
        return bus();
    }

    public LegacySide getEffectiveSide() {
        return FMLEnvironment.dist == Dist.CLIENT ? LegacySide.CLIENT : LegacySide.SERVER;
    }

    public LegacySide getSide() {
        return getEffectiveSide();
    }

    public LegacySide func_71562_b() {
        return getEffectiveSide();
    }

    public void exitJava(int exitCode, boolean halt) {
        System.err.println("LegacyFMLCommonHandler: Mod requested exitJava with code " + exitCode);
    }

    public void registerCrashCallable(LegacyICrashCallable callable) {
    }

    public void enhanceCrashReport(Object crashReport, Object category) {
    }
}
