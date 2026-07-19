package jp.ngt.mccompat;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * パックスクリプト互換: 1.7.10 の net.minecraft.client.renderer.texture.TextureMap。
 * ブロックアトラスの位置を静的フィールドとして返す (現行の TextureAtlas.LOCATION_BLOCKS)。
 * SRG 名 (field_110575_b) と MCP 名 (locationBlocksTexture) の両方を用意する。
 */
public final class TextureMap {
    public static final ResourceLocation locationBlocksTexture = TextureAtlas.LOCATION_BLOCKS;
    public static final ResourceLocation field_110575_b = TextureAtlas.LOCATION_BLOCKS;

    private TextureMap() {
    }
}
