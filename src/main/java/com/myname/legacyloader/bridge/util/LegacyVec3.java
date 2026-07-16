package com.myname.legacyloader.bridge.util;

/**
 * Bridge for 1.7.10 Vec3 class.
 * Modern equivalent is net.minecraft.world.phys.Vec3.
 */
public class LegacyVec3 extends net.minecraft.world.phys.Vec3 {

    public LegacyVec3(double x, double y, double z) {
        super(x, y, z);
    }

    // Legacy factory methods
    public static LegacyVec3 createVectorHelper(double x, double y, double z) {
        return new LegacyVec3(x, y, z);
    }

    // SRG alias
    public static LegacyVec3 func_72443_a(double x, double y, double z) {
        return createVectorHelper(x, y, z);
    }

    // 1.7.10 accessed xCoord/yCoord/zCoord as public fields; ClassTransformer remaps
    // those field accesses to x/y/z on net.minecraft.world.phys.Vec3 at load time.
}
