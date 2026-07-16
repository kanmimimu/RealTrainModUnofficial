package com.myname.legacyloader.bridge.client.renderer.item;

import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal 1.7.10 MinecraftForgeClient item-renderer bridge.
 *
 * BuildCraft registers custom IItemRenderer instances for pipes, gates, facades and several
 * machine-like inventory models.  Ignoring those registrations makes the creative tab fall back
 * to missing/barrier models.  The real 1.20 model pipeline still needs baking hooks elsewhere,
 * but keeping this registry allows model capture / future dynamic render fallback to find the
 * legacy renderer instead of losing that information at registration time.
 */
public class LegacyMinecraftForgeClient {

    private static final Map<Item, LegacyIItemRenderer> ITEM_RENDERERS = new ConcurrentHashMap<>();

    public static void registerItemRenderer(Item item, LegacyIItemRenderer renderer) {
        if (item == null || renderer == null) return;
        ITEM_RENDERERS.put(item, renderer);
    }

    public static LegacyIItemRenderer getItemRenderer(Item item, LegacyItemRenderType type) {
        if (item == null) return null;
        LegacyIItemRenderer renderer = ITEM_RENDERERS.get(item);
        if (renderer == null) return null;
        try {
            return renderer.handleRenderType(null, type) ? renderer : null;
        } catch (Throwable ignored) {
            return renderer;
        }
    }

    public static LegacyIItemRenderer getItemRenderer(Item item) {
        return item == null ? null : ITEM_RENDERERS.get(item);
    }

    public static Map<Item, LegacyIItemRenderer> getRegisteredItemRenderers() {
        return ITEM_RENDERERS;
    }
}
