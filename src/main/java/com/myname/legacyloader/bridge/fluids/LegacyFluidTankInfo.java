package com.myname.legacyloader.bridge.fluids;

public class LegacyFluidTankInfo {
    public LegacyFluidStack fluid;
    public int capacity;

    public LegacyFluidTankInfo(LegacyFluidStack fluid, int capacity) {
        this.fluid = fluid;
        this.capacity = capacity;
    }

    public LegacyFluidTankInfo(LegacyFluidTank tank) {
        this(tank == null ? null : tank.getFluid(), tank == null ? 0 : tank.getCapacity());
    }
}
