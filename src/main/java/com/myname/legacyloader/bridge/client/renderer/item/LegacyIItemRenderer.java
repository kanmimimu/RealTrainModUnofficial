package com.myname.legacyloader.bridge.client.renderer.item;

import net.minecraft.world.item.ItemStack;

public interface LegacyIItemRenderer {

    boolean handleRenderType(ItemStack item, LegacyItemRenderType type);

    boolean shouldUseRenderHelper(LegacyItemRenderType type, ItemStack item, LegacyItemRendererHelper helper);

    void renderItem(LegacyItemRenderType type, ItemStack item, Object... data);
}