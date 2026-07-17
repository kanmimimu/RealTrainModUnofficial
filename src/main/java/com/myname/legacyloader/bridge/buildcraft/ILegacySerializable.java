package com.myname.legacyloader.bridge.buildcraft;

import net.minecraft.nbt.CompoundTag;

/**
 * Stub for BuildCraft's ISerializable interface.
 */
public interface ILegacySerializable {
    void writeToNBT(CompoundTag tag);
    void readFromNBT(CompoundTag tag);
}
