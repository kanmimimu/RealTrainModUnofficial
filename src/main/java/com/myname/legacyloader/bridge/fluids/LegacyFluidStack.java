package com.myname.legacyloader.bridge.fluids;

import net.minecraft.nbt.CompoundTag;

public class LegacyFluidStack {
    public LegacyFluid fluid;
    public int amount;

    public LegacyFluidStack(LegacyFluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public LegacyFluid getFluid() { return fluid; }
    public boolean isFluidEqual(LegacyFluidStack other) { return other != null && other.getFluid() == getFluid(); }
    public boolean containsFluid(LegacyFluidStack other) { return other != null && isFluidEqual(other) && amount >= other.amount; }
    public boolean isFluidStackIdentical(LegacyFluidStack other) { return other != null && isFluidEqual(other) && amount == other.amount; }
    public LegacyFluidStack copy() { return new LegacyFluidStack(fluid, amount); }
    public String getLocalizedName() { return fluid == null ? "" : fluid.getLocalizedName(this); }

    public CompoundTag writeToNBT(CompoundTag tag) {
        if (tag != null && fluid != null) {
            tag.putString("FluidName", fluid.getName());
            tag.putInt("Amount", amount);
        }
        return tag;
    }

    public static LegacyFluidStack loadFluidStackFromNBT(CompoundTag tag) {
        if (tag == null) return null;
        LegacyFluid fluid = LegacyFluidRegistry.getFluid(tag.getString("FluidName"));
        return fluid == null ? null : new LegacyFluidStack(fluid, tag.getInt("Amount"));
    }
}
