package com.myname.legacyloader.bridge.registry;

import com.myname.legacyloader.bridge.core.RegistryNameHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BridgeRegistry<T> {
    private final Registry<T> realRegistry;

    public BridgeRegistry(Registry<T> realRegistry) {
        this.realRegistry = realRegistry;
    }

    public void register(T value) {
        ResourceLocation name = RegistryNameHelper.getRegistryName(value);
        if (name != null) {
            Registry.register(realRegistry, name, value);
        } else {
            System.err.println("LegacyLoader Warning: Attempted to register object without a name: " + value);
        }
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return realRegistry.key();
    }
}
