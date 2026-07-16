package com.myname.legacyloader.bridge.inventory;

import net.minecraft.world.item.ItemStack;

public interface LegacySidedInventory extends LegacyInventory {
    default int[] func_94128_d(int side) {
        int size = Math.max(0, func_70302_i_());
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return slots;
    }

    default int[] getAccessibleSlotsFromSide(int side) {
        return func_94128_d(side);
    }

    default boolean func_102007_a(int slot, ItemStack stack, int side) {
        return func_94041_b(slot, stack);
    }

    default boolean canInsertItem(int slot, ItemStack stack, int side) {
        return func_102007_a(slot, stack, side);
    }

    default boolean func_102008_b(int slot, ItemStack stack, int side) {
        return true;
    }

    default boolean canExtractItem(int slot, ItemStack stack, int side) {
        return func_102008_b(slot, stack, side);
    }
}
