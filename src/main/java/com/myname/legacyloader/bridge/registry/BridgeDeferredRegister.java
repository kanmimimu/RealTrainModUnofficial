package com.myname.legacyloader.bridge.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BridgeDeferredRegister<T> {
    private final DeferredRegister<T> delegate;

    private BridgeDeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        this.delegate = DeferredRegister.create(registryKey, modid);
        com.myname.legacyloader.LegacyLoaderMod.CURRENT_LOADING_MODID = modid;
    }

    public static <B> BridgeDeferredRegister<B> create(BridgeRegistry<B> registry, String modid) {
        return new BridgeDeferredRegister<>(registry.getRegistryKey(), modid);
    }

    public static <B> BridgeDeferredRegister<B> create(Class<B> base, String modid) {
        ResourceKey<? extends Registry<B>> key;
        if (base.getSimpleName().contains("Item")) {
            key = (ResourceKey<? extends Registry<B>>) (Object) Registries.ITEM;
        } else {
            key = (ResourceKey<? extends Registry<B>>) (Object) Registries.BLOCK;
        }
        return new BridgeDeferredRegister<>(key, modid);
    }

    public static <B> BridgeDeferredRegister<B> create(Object obj, String modid) {
        if (obj instanceof BridgeRegistry) {
            return create((BridgeRegistry<B>) obj, modid);
        }
        if (obj instanceof Class) {
            return create((Class<B>) obj, modid);
        }
        return new BridgeDeferredRegister<>((ResourceKey<? extends Registry<B>>) (Object) Registries.BLOCK, modid);
    }

    public DeferredHolder<T, ? extends T> register(String name, java.util.function.Supplier<? extends T> supplier) {
        return delegate.register(name, supplier);
    }

    public void register(IEventBus bus) {
        delegate.register(bus);
    }
}
