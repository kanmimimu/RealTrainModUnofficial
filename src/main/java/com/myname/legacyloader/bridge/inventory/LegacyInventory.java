package com.myname.legacyloader.bridge.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface LegacyInventory {
    default int func_70302_i_() {
        return 0;
    }

    default int getSizeInventory() {
        return func_70302_i_();
    }

    default ItemStack func_70301_a(int slot) {
        return null;
    }

    default ItemStack getStackInSlot(int slot) {
        return func_70301_a(slot);
    }

    default ItemStack func_70298_a(int slot, int amount) {
        return null;
    }

    default ItemStack decrStackSize(int slot, int amount) {
        return func_70298_a(slot, amount);
    }

    default ItemStack func_70304_b(int slot) {
        return null;
    }

    default ItemStack getStackInSlotOnClosing(int slot) {
        return func_70304_b(slot);
    }

    default void func_70299_a(int slot, ItemStack stack) {
    }

    default void setInventorySlotContents(int slot, ItemStack stack) {
        func_70299_a(slot, stack);
    }

    default String func_145825_b() {
        return "container";
    }

    default String getInventoryName() {
        return func_145825_b();
    }

    default boolean func_145818_k_() {
        return false;
    }

    default boolean hasCustomInventoryName() {
        return func_145818_k_();
    }

    default int func_70297_j_() {
        return 64;
    }

    default int getInventoryStackLimit() {
        return func_70297_j_();
    }

    default void func_70296_d() {
    }

    default void markDirty() {
        func_70296_d();
    }

    default boolean func_70300_a(Player player) {
        return true;
    }

    default boolean isUseableByPlayer(Player player) {
        return func_70300_a(player);
    }

    default void func_70295_k_() {
    }

    default void openInventory() {
        func_70295_k_();
    }

    default void func_70305_f() {
    }

    default void closeInventory() {
        func_70305_f();
    }

    default boolean func_94041_b(int slot, ItemStack stack) {
        return true;
    }

    default boolean isItemValidForSlot(int slot, ItemStack stack) {
        return func_94041_b(slot, stack);
    }
}
