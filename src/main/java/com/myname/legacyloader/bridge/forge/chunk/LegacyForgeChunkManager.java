package com.myname.legacyloader.bridge.forge.chunk;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class LegacyForgeChunkManager {

    // 繧ｳ繝ｼ繝ｫ繝舌ャ繧ｯ縺ｮ逋ｻ骭ｲ
    public static void setForcedChunkLoadingCallback(Object mod, LegacyForgeChunkManagerLoadingCallback callback) {
        // System.out.println("LegacyLoader: Ignored ChunkLoadingCallback for " + mod);
    }

    // 繝√こ繝・ヨ縺ｮ隕∵ｱ・
    public static LegacyTicket requestTicket(Object mod, Level world, LegacyChunkManagerType type) {
        // 繝繝溘・縺ｮ繝√こ繝・ヨ繧定ｿ斐☆
        return new LegacyTicket();
    }

    // 縺昴・莉門ｿ・ｦ√↓縺ｪ繧翫◎縺・↑繝｡繧ｽ繝・ラ
    public static void releaseTicket(LegacyTicket ticket) {}

    public static void forceChunk(LegacyTicket ticket, ChunkPos chunk) {}

    public static void unforceChunk(LegacyTicket ticket, ChunkPos chunk) {}
}
