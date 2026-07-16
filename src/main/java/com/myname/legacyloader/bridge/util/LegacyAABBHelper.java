package com.myname.legacyloader.bridge.util;

import net.minecraft.world.phys.AABB;

public final class LegacyAABBHelper {
    private LegacyAABBHelper() {
    }

    public static AABB func_72330_a(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
