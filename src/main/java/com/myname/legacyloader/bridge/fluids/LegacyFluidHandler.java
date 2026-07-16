package com.myname.legacyloader.bridge.fluids;

import com.myname.legacyloader.bridge.forge.LegacyForgeDirection;

public interface LegacyFluidHandler {
    default int fill(LegacyForgeDirection from, LegacyFluidStack resource, boolean doFill) { return 0; }
    default LegacyFluidStack drain(LegacyForgeDirection from, LegacyFluidStack resource, boolean doDrain) { return null; }
    default LegacyFluidStack drain(LegacyForgeDirection from, int maxDrain, boolean doDrain) { return null; }
    default boolean canFill(LegacyForgeDirection from, LegacyFluid fluid) { return false; }
    default boolean canDrain(LegacyForgeDirection from, LegacyFluid fluid) { return false; }
    default LegacyFluidTankInfo[] getTankInfo(LegacyForgeDirection from) { return new LegacyFluidTankInfo[0]; }
}
