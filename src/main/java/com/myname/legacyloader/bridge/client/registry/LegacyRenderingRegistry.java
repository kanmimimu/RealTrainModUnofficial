package com.myname.legacyloader.bridge.client.registry;

import com.myname.legacyloader.bridge.client.renderer.entity.LegacyRender;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LegacyRenderingRegistry {

    private static int nextId = 1000;
    private static int nextArmorId = 5;
    private static final Map<Integer, LegacySimpleBlockRenderingHandler> BLOCK_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<Integer, String> HANDLER_CLASS_NAMES = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> ENTITY_RENDERERS = new ConcurrentHashMap<>();

    public static int getNextAvailableRenderId() {
        return nextId++;
    }

    public static void registerBlockHandler(LegacySimpleBlockRenderingHandler handler) {
        if (handler == null) return;
        int renderId = safeGetRenderId(handler);
        if (renderId <= 0) renderId = nextId++;
        registerBlockHandler(renderId, handler);
    }

    public static void registerBlockHandler(int renderId, LegacySimpleBlockRenderingHandler handler) {
        if (handler == null) return;
        BLOCK_HANDLERS.put(renderId, handler);
        HANDLER_CLASS_NAMES.put(renderId, handler.getClass().getName());
    }

    public static LegacySimpleBlockRenderingHandler getBlockHandler(int renderId) {
        return BLOCK_HANDLERS.get(renderId);
    }

    public static String getBlockHandlerClassName(int renderId) {
        return HANDLER_CLASS_NAMES.get(renderId);
    }

    public static int getRenderType(Block block) {
        if (block == null) return 0;
        try {
            Method method = block.getClass().getMethod("func_149645_b");
            Object result = method.invoke(block);
            if (result instanceof Number number) return number.intValue();
        } catch (Throwable ignored) {
        }
        return 0;
    }

    public static String getRenderHandlerClassName(Block block) {
        return getBlockHandlerClassName(getRenderType(block));
    }

    public static int addNewArmourRendererPrefix(String prefix) {
        return nextArmorId++;
    }

    public static void registerEntityRenderingHandler(Class<?> entityClass, LegacyRender renderer) {
        if (entityClass != null && renderer != null) ENTITY_RENDERERS.put(entityClass, renderer);
    }

    public static void registerEntityRenderingHandler(Class<?> entityClass, Object renderer) {
        if (entityClass != null && renderer != null) ENTITY_RENDERERS.put(entityClass, renderer);
    }

    public static Object getEntityRenderer(Class<?> entityClass) {
        return entityClass == null ? null : ENTITY_RENDERERS.get(entityClass);
    }

    public static int func_72762_a(String prefix) {
        return addNewArmourRendererPrefix(prefix);
    }

    private static int safeGetRenderId(LegacySimpleBlockRenderingHandler handler) {
        try {
            return handler.getRenderId();
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
