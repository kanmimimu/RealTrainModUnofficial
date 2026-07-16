package com.myname.legacyloader.bridge.fluids;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LegacyFluidContainerRegistry {
    public static final int BUCKET_VOLUME = 1000;
    public static final ItemStack EMPTY_BUCKET = ItemStack.EMPTY;
    public static final ItemStack EMPTY_BOTTLE = ItemStack.EMPTY;

    private static final List<FluidContainerData> CONTAINERS = Collections.synchronizedList(new ArrayList<>());

    private LegacyFluidContainerRegistry() {}

    public static boolean registerFluidContainer(LegacyFluidStack fluid, ItemStack filled, ItemStack empty) {
        if (fluid == null || fluid.getFluid() == null || filled == null || filled.isEmpty()) return false;
        ItemStack filledCopy = filled.copy();
        ItemStack emptyCopy = empty == null ? ItemStack.EMPTY : empty.copy();
        synchronized (CONTAINERS) {
            for (FluidContainerData data : CONTAINERS) {
                if (ItemStack.isSameItemSameComponents(data.filledContainer, filledCopy)) return false;
            }
            CONTAINERS.add(new FluidContainerData(fluid.copy(), filledCopy, emptyCopy));
        }
        return true;
    }

    public static boolean registerFluidContainer(LegacyFluid fluid, ItemStack filled, ItemStack empty) {
        return fluid != null && registerFluidContainer(new LegacyFluidStack(fluid, BUCKET_VOLUME), filled, empty);
    }

    public static boolean registerFluidContainer(LegacyFluidStack fluid, ItemStack filled) {
        return registerFluidContainer(fluid, filled, ItemStack.EMPTY);
    }

    public static boolean registerFluidContainer(LegacyFluid fluid, ItemStack filled) {
        return fluid != null && registerFluidContainer(new LegacyFluidStack(fluid, BUCKET_VOLUME), filled, ItemStack.EMPTY);
    }

    public static boolean isContainer(ItemStack stack) { return isEmptyContainer(stack) || isFilledContainer(stack); }
    public static boolean isBucket(ItemStack stack) { return isContainer(stack); }

    public static boolean isEmptyContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        synchronized (CONTAINERS) {
            for (FluidContainerData data : CONTAINERS) {
                if (!data.emptyContainer.isEmpty() && ItemStack.isSameItemSameComponents(data.emptyContainer, stack)) return true;
            }
        }
        return false;
    }

    public static boolean isFilledContainer(ItemStack stack) { return getFluidForFilledItem(stack) != null; }

    public static LegacyFluidStack getFluidForFilledItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        synchronized (CONTAINERS) {
            for (FluidContainerData data : CONTAINERS) {
                if (ItemStack.isSameItemSameComponents(data.filledContainer, stack)) return data.fluid.copy();
            }
        }
        return null;
    }

    public static ItemStack fillFluidContainer(LegacyFluidStack fluid, ItemStack emptyContainer) {
        if (fluid == null || emptyContainer == null || emptyContainer.isEmpty()) return null;
        synchronized (CONTAINERS) {
            for (FluidContainerData data : CONTAINERS) {
                if (data.fluid != null && data.fluid.containsFluid(fluid)
                        && !data.emptyContainer.isEmpty()
                        && ItemStack.isSameItemSameComponents(data.emptyContainer, emptyContainer)) {
                    return data.filledContainer.copy();
                }
            }
        }
        return null;
    }

    public static ItemStack drainFluidContainer(ItemStack filledContainer) {
        if (filledContainer == null || filledContainer.isEmpty()) return null;
        synchronized (CONTAINERS) {
            for (FluidContainerData data : CONTAINERS) {
                if (ItemStack.isSameItemSameComponents(data.filledContainer, filledContainer)) {
                    return data.emptyContainer.isEmpty() ? ItemStack.EMPTY : data.emptyContainer.copy();
                }
            }
        }
        return null;
    }

    public static int getContainerCapacity(ItemStack stack) {
        LegacyFluidStack fluid = getFluidForFilledItem(stack);
        return fluid != null ? fluid.amount : 0;
    }

    public static int getContainerCapacity(LegacyFluidStack fluid, ItemStack emptyContainer) {
        ItemStack filled = fillFluidContainer(fluid, emptyContainer);
        LegacyFluidStack contained = getFluidForFilledItem(filled);
        return contained != null ? contained.amount : 0;
    }

    public static boolean containsFluid(ItemStack container, LegacyFluidStack fluid) {
        LegacyFluidStack contained = getFluidForFilledItem(container);
        return contained != null && contained.containsFluid(fluid);
    }

    public static FluidContainerData[] getRegisteredFluidContainerData() {
        synchronized (CONTAINERS) { return CONTAINERS.toArray(new FluidContainerData[0]); }
    }

    public static final class FluidContainerData {
        public final LegacyFluidStack fluid;
        public final ItemStack filledContainer;
        public final ItemStack emptyContainer;
        public FluidContainerData(LegacyFluidStack fluid, ItemStack filledContainer, ItemStack emptyContainer) {
            this.fluid = fluid;
            this.filledContainer = filledContainer;
            this.emptyContainer = emptyContainer;
        }
    }
}
