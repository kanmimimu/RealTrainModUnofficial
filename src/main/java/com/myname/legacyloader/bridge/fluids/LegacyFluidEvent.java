package com.myname.legacyloader.bridge.fluids;

import com.myname.legacyloader.bridge.fml.LegacyEvent;
import net.minecraft.world.level.Level;

public class LegacyFluidEvent extends LegacyEvent {
    public final LegacyFluidStack fluid;
    public final Level world;
    public final int x;
    public final int y;
    public final int z;

    public LegacyFluidEvent(LegacyFluidStack fluid, Level world, int x, int y, int z) {
        this.fluid = fluid;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static boolean fireEvent(LegacyFluidEvent event) {
        return false;
    }

    public static class FluidSpilledEvent extends LegacyFluidEvent {
        public FluidSpilledEvent(LegacyFluidStack fluid, Level world, int x, int y, int z) {
            super(fluid, world, x, y, z);
        }
    }
}
