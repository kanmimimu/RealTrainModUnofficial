package com.myname.legacyloader.bridge.dispenser;

public class LegacyPositionImpl implements LegacyIPosition {
    private final double x;
    private final double y;
    private final double z;

    public LegacyPositionImpl(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getZ() { return this.z; }
}
