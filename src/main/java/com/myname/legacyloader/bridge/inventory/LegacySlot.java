package com.myname.legacyloader.bridge.inventory;

public class LegacySlot {
    public LegacySlot() {}
    public LegacySlot(Object inventory, int index, int x, int y) {}
    public boolean isItemValid(Object stack) { return true; }
    public Object getStack() { return null; }
    public void putStack(Object stack) {}
}
