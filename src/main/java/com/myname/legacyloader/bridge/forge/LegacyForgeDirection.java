package com.myname.legacyloader.bridge.forge;

public enum LegacyForgeDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0),
    UNKNOWN(0, 0, 0);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;
    public static final LegacyForgeDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};

    LegacyForgeDirection(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.flag = 1 << this.ordinal();
    }

    // 1.7.10莠呈鋤繝｡繧ｽ繝・ラ
    public static LegacyForgeDirection getOrientation(int id) {
        if (id >= 0 && id < VALID_DIRECTIONS.length) {
            return VALID_DIRECTIONS[id];
        }
        return UNKNOWN;
    }

    public LegacyForgeDirection getOpposite() {
        return getOrientation(ordinal() ^ 1);
    }
}