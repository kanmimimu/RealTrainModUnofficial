package com.myname.legacyloader.bridge.client.registry;

import com.myname.legacyloader.bridge.client.renderer.LegacyRenderBlocks;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

public interface LegacySimpleBlockRenderingHandler {

    // 1.7.10: renderInventoryBlock(Block, int metadata, int modelId, RenderBlocks)
    void renderInventoryBlock(Block block, int metadata, int modelId, LegacyRenderBlocks renderer);

    // 1.7.10: renderWorldBlock(IBlockAccess, int x, int y, int z, Block, int modelId, RenderBlocks)
    // IBlockAccess -> BlockGetter 縺ｫ繝槭ャ繝励＠縺ｾ縺・
    boolean renderWorldBlock(BlockGetter world, int x, int y, int z, Block block, int modelId, LegacyRenderBlocks renderer);

    boolean shouldRender3DInInventory(int modelId);

    int getRenderId();
}