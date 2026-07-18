package com.myname.legacyloader.bridge.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.WeakHashMap;

public class RegistryNameHelper {
    // 繧ｪ繝悶ず繧ｧ繧ｯ繝・Block縺ｪ縺ｩ)縺ｨ縲√◎縺ｮ蜷榊燕(ResourceLocation)縺ｮ蟇ｾ蠢懆｡ｨ
    private static final Map<Object, ResourceLocation> REGISTRY_NAMES = new WeakHashMap<>();

    public static void setRegistryName(Object obj, ResourceLocation name) {
        REGISTRY_NAMES.put(obj, name);
    }

    public static ResourceLocation getRegistryName(Object obj) {
        return REGISTRY_NAMES.get(obj);
    }
}