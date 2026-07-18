package com.myname.legacyloader.bridge.fml;

import com.myname.legacyloader.bridge.world.IChunkProvider;
import net.minecraft.world.level.Level;
import java.util.Random;

public interface IWorldGenerator {
    // 1.7.10縺ｮ繧ｷ繧ｰ繝阪メ繝｣: (Random, int, int, World, IChunkProvider, IChunkProvider)
    // World 縺ｯ 1.20.1 縺ｮ Level 縺ｫ繝槭ャ繝斐Φ繧ｰ縺輔ｌ繧九◆繧√√％縺薙〒縺ｯ Level 繧剃ｽｿ逕ｨ縺励∪縺・
    void generate(Random random, int chunkX, int chunkZ, Level world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider);
}