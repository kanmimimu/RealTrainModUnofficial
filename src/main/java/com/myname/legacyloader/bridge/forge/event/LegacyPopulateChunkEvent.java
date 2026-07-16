package com.myname.legacyloader.bridge.forge.event;

import com.myname.legacyloader.bridge.fml.LegacyEvent;

import java.util.Random;

public class LegacyPopulateChunkEvent extends LegacyEvent {
    public final Object chunkProvider;
    public final Object world;
    public final Random rand;
    public final int chunkX;
    public final int chunkZ;
    public final boolean hasVillageGenerated;

    public LegacyPopulateChunkEvent(Object chunkProvider, Object world, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated) {
        this.chunkProvider = chunkProvider;
        this.world = world;
        this.rand = rand;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.hasVillageGenerated = hasVillageGenerated;
    }

    public static class Pre extends LegacyPopulateChunkEvent {
        public Pre(Object chunkProvider, Object world, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated) {
            super(chunkProvider, world, rand, chunkX, chunkZ, hasVillageGenerated);
        }
    }

    public static class Post extends LegacyPopulateChunkEvent {
        public Post(Object chunkProvider, Object world, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated) {
            super(chunkProvider, world, rand, chunkX, chunkZ, hasVillageGenerated);
        }
    }

    public static class Populate extends LegacyPopulateChunkEvent {
        public final EventType type;

        public Populate(Object chunkProvider, Object world, Random rand, int chunkX, int chunkZ, boolean hasVillageGenerated, EventType type) {
            super(chunkProvider, world, rand, chunkX, chunkZ, hasVillageGenerated);
            this.type = type;
        }

        public enum EventType {
            LAKE,
            LAVA,
            DUNGEON,
            ANIMALS,
            ICE,
            CUSTOM
        }
    }
}
