package com.myname.legacyloader.bridge.inventory;

import net.minecraft.world.Container;

/**
 * Bridge for 1.7.10 SlotFurnaceOutput.
 */
public class LegacySlotFurnaceOutput extends LegacySlot {
    public LegacySlotFurnaceOutput(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
}
