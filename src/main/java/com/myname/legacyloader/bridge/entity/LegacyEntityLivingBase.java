package com.myname.legacyloader.bridge.entity;

public class LegacyEntityLivingBase extends LegacyEntity {
    public float getHealth() { return 20.0F; }
    public void setHealth(float value) {}
    public boolean attackEntityFrom(Object source, float amount) { return false; }
    public void heal(float amount) {}
}
