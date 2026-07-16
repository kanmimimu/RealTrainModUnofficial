package com.myname.legacyloader.bridge.fluids;

import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public final class LegacyFluidRegistry {
    private static final Map<String, LegacyFluid> BY_NAME = new HashMap<>();
    public static final LegacyFluid WATER = registerBuiltin(new LegacyFluid("water"));
    public static final LegacyFluid LAVA = registerBuiltin(new LegacyFluid("lava"));

    private LegacyFluidRegistry() {}

    private static LegacyFluid registerBuiltin(LegacyFluid fluid) {
        registerFluid(fluid);
        return fluid;
    }

    public static boolean registerFluid(LegacyFluid fluid) {
        if (fluid == null) return false;
        BY_NAME.putIfAbsent(fluid.getName(), fluid);
        return true;
    }

    public static boolean isFluidRegistered(String name) { return BY_NAME.containsKey(name); }
    public static LegacyFluid getFluid(String name) { return BY_NAME.get(name); }

    public static LegacyFluid getFluid(int id) {
        for (LegacyFluid fluid : BY_NAME.values()) if (fluid.getID() == id) return fluid;
        return null;
    }

    public static LegacyFluidStack getFluidStack(String name, int amount) {
        LegacyFluid fluid = getFluid(name);
        return fluid == null ? null : new LegacyFluidStack(fluid, amount);
    }

    public static LegacyFluid lookupFluidForBlock(Block block) {
        for (LegacyFluid fluid : BY_NAME.values()) if (fluid.getBlock() == block) return fluid;
        return null;
    }
}
