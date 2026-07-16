package com.myname.legacyloader.bridge.buildcraft;

/**
 * Stub for BuildCraft's IPipeTile interface.
 */
public interface LegacyIPipeTile {
    enum PipeType {
        ITEM, FLUID, POWER, STRUCTURE
    }

    PipeType getPipeType();
    boolean isPipeConnected(Object direction);
}
