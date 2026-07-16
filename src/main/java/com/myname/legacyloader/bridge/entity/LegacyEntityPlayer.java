package com.myname.legacyloader.bridge.entity;

public class LegacyEntityPlayer extends LegacyEntityLivingBase {
    public Object field_71071_by; // inventory
    public String getCommandSenderName() { return "LegacyPlayer"; }
    public void addChatMessage(Object component) {}
    public boolean canPlayerEdit(int x, int y, int z, int side, Object stack) { return true; }
}
