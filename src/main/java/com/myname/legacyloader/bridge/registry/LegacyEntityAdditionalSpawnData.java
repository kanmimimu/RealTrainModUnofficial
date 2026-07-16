package com.myname.legacyloader.bridge.registry;

import io.netty.buffer.ByteBuf;

public interface LegacyEntityAdditionalSpawnData {
    default void writeSpawnData(ByteBuf buffer) {
    }

    default void readSpawnData(ByteBuf additionalData) {
    }
}
