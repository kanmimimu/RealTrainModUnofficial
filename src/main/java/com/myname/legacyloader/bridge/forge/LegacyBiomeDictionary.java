package com.myname.legacyloader.bridge.forge;

import com.myname.legacyloader.bridge.world.biome.LegacyBiomeGenBase;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class LegacyBiomeDictionary {
    private static final Map<Type, List<LegacyBiomeGenBase>> BIOMES = new EnumMap<>(Type.class);

    private LegacyBiomeDictionary() {
    }

    public static boolean registerBiomeType(LegacyBiomeGenBase biome, Type... types) {
        if (biome == null || types == null) {
            return false;
        }
        for (Type type : types) {
            if (type != null) {
                BIOMES.computeIfAbsent(type, ignored -> new ArrayList<>()).add(biome);
            }
        }
        return true;
    }

    public static LegacyBiomeGenBase[] getBiomesForType(Type type) {
        List<LegacyBiomeGenBase> biomes = BIOMES.get(type);
        if (biomes == null) {
            return new LegacyBiomeGenBase[0];
        }
        return biomes.toArray(new LegacyBiomeGenBase[0]);
    }

    public enum Type {
        HOT,
        COLD,
        SPARSE,
        DENSE,
        WET,
        DRY,
        SAVANNA,
        CONIFEROUS,
        JUNGLE,
        SPOOKY,
        DEAD,
        LUSH,
        NETHER,
        END,
        MUSHROOM,
        MAGICAL,
        OCEAN,
        RIVER,
        WATER,
        MESA,
        FOREST,
        PLAINS,
        MOUNTAIN,
        HILLS,
        SWAMP,
        SANDY,
        SNOWY,
        WASTELAND,
        BEACH
    }
}
