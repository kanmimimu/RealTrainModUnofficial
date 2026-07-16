package com.myname.legacyloader.bridge.fluids;

public interface LegacyFluidTank {
    LegacyFluidStack getFluid();
    int getFluidAmount();
    int getCapacity();
    LegacyFluidTankInfo getInfo();
    int fill(LegacyFluidStack resource, boolean doFill);
    LegacyFluidStack drain(int maxDrain, boolean doDrain);
}
