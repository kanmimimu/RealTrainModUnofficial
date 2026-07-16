package com.myname.legacyloader.bridge.world.gen;

/**
 * Stub for 1.7.10 StructureBoundingBox.
 * Modern equivalent is net.minecraft.world.level.levelgen.structure.BoundingBox.
 */
public class LegacyStructureBoundingBox {
    public int minX, minY, minZ, maxX, maxY, maxZ;

    public LegacyStructureBoundingBox() {}

    public LegacyStructureBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean isVecInside(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
