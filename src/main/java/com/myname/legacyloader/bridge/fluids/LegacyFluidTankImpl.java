package com.myname.legacyloader.bridge.fluids;

import com.myname.legacyloader.bridge.tileentity.LegacyTileEntity;
import net.minecraft.nbt.CompoundTag;

public class LegacyFluidTankImpl implements LegacyFluidTank {
    protected LegacyFluidStack fluid;
    protected int capacity;
    public LegacyTileEntity tile;

    public LegacyFluidTankImpl(int capacity) { this.capacity = capacity; }
    public LegacyFluidTankImpl(LegacyFluidStack fluid, int capacity) { this.fluid = fluid; this.capacity = capacity; }
    public LegacyFluidTankImpl(LegacyFluid fluid, int amount, int capacity) { this(new LegacyFluidStack(fluid, amount), capacity); }

    @Override public LegacyFluidStack getFluid() { return fluid; }
    public void setFluid(LegacyFluidStack fluid) { this.fluid = fluid; }
    @Override public int getFluidAmount() { return fluid == null ? 0 : fluid.amount; }
    @Override public int getCapacity() { return capacity; }
    @Override public LegacyFluidTankInfo getInfo() { return new LegacyFluidTankInfo(this); }

    @Override
    public int fill(LegacyFluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) return 0;
        int accepted = Math.max(0, Math.min(resource.amount, capacity - getFluidAmount()));
        if (doFill && accepted > 0) {
            if (fluid == null) fluid = new LegacyFluidStack(resource.getFluid(), accepted);
            else fluid.amount += accepted;
        }
        return accepted;
    }

    @Override
    public LegacyFluidStack drain(int maxDrain, boolean doDrain) {
        if (fluid == null || maxDrain <= 0) return null;
        int drained = Math.min(maxDrain, fluid.amount);
        LegacyFluidStack result = new LegacyFluidStack(fluid.getFluid(), drained);
        if (doDrain) {
            fluid.amount -= drained;
            if (fluid.amount <= 0) fluid = null;
        }
        return result;
    }

    public LegacyFluidTankImpl readFromNBT(CompoundTag tag) {
        fluid = LegacyFluidStack.loadFluidStackFromNBT(tag);
        return this;
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        if (fluid != null && tag != null) fluid.writeToNBT(tag);
        return tag;
    }
}
