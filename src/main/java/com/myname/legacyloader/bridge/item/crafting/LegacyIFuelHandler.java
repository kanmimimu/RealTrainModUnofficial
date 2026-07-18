package com.myname.legacyloader.bridge.item.crafting;
import net.minecraft.world.item.ItemStack;
public interface LegacyIFuelHandler {
    int getBurnTime(ItemStack fuel);
}