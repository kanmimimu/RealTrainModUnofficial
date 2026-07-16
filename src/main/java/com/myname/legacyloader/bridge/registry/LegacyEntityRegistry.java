package com.myname.legacyloader.bridge.registry;

import com.myname.legacyloader.bridge.entity.LegacyEntityList;

public final class LegacyEntityRegistry {
    private LegacyEntityRegistry() {
    }

    public static void registerModEntity(Class<?> entityClass, String name, int id, Object mod, int trackingRange,
                                         int updateFrequency, boolean sendsVelocityUpdates) {
        LegacyEntityList.addMapping(entityClass, name, id);
    }
}
