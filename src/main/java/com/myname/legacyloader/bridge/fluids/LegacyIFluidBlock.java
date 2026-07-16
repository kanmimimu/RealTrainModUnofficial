package com.myname.legacyloader.bridge.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface LegacyIFluidBlock {
    LegacyFluid getFluid();
    float getFilledPercentage(Level world, BlockPos pos);
}
