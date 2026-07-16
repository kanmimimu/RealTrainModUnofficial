package com.myname.legacyloader.bridge.forge.event;

import java.util.Random;

public final class LegacyTerrainGen {
    private LegacyTerrainGen() {
    }

    public static boolean populate(Object chunkProvider, Object world, Random rand, int chunkX, int chunkZ,
                                   boolean hasVillageGenerated,
                                   LegacyPopulateChunkEvent.Populate.EventType eventType) {
        return true;
    }
}
