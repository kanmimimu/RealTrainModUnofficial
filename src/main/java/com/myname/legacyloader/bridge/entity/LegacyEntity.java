package com.myname.legacyloader.bridge.entity;

public class LegacyEntity {
    public static int field_70152_a;

    public double field_70165_t, field_70163_u, field_70161_v; // posX/Y/Z
    public double field_70159_w, field_70181_x, field_70179_y; // motionX/Y/Z
    public float field_70177_z, field_70125_A; // rotationYaw/Pitch
    public boolean field_70128_L = false; // isDead
    public Object field_70170_p; // worldObj
    public void setDead() { field_70128_L = true; }
    public boolean isEntityAlive() { return !field_70128_L; }
    public void setPosition(double x, double y, double z) { field_70165_t=x; field_70163_u=y; field_70161_v=z; }
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) { setPosition(x,y,z); field_70177_z=yaw; field_70125_A=pitch; }
    public double getDistanceSq(double x, double y, double z) { double dx=field_70165_t-x, dy=field_70163_u-y, dz=field_70161_v-z; return dx*dx+dy*dy+dz*dz; }
}
