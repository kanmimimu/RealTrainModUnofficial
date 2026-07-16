package com.myname.legacyloader.bridge.fml;

import net.minecraft.world.item.ItemStack;

public interface LegacyIFuelHandler {
    int getBurnTime(ItemStack fuel);
}