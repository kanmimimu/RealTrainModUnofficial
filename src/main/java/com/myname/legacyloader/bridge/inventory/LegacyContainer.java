package com.myname.legacyloader.bridge.inventory;

public class LegacyContainer {
    public int field_75152_c;
    public boolean canInteractWith(Object player) { return true; }
    public Object addSlotToContainer(Object slot) { return slot; }
    public Object transferStackInSlot(Object player, int index) { return null; }
    public void detectAndSendChanges() {}
}
