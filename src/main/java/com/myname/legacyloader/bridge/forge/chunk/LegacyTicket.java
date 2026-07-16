package com.myname.legacyloader.bridge.forge.chunk;

import net.minecraft.nbt.CompoundTag;

public class LegacyTicket {
    // 繝・・繧ｿ菫晏ｭ倡畑縺ｮNBT
    private CompoundTag modData = new CompoundTag();

    public CompoundTag getModData() {
        return modData;
    }
}