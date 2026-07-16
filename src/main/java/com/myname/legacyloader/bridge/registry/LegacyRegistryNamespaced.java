package com.myname.legacyloader.bridge.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Iterator;

public class LegacyRegistryNamespaced<T> implements Iterable<T> {
    public static final LegacyRegistryNamespaced<Block> BLOCKS = new LegacyRegistryNamespaced<>(BuiltInRegistries.BLOCK);
    public static final LegacyRegistryNamespaced<Item> ITEMS = new LegacyRegistryNamespaced<>(BuiltInRegistries.ITEM);

    private final Registry<T> registry;

    private LegacyRegistryNamespaced(Registry<T> registry) {
        this.registry = registry;
    }

    public String func_148750_c(Object value) {
        ResourceLocation key = registry.getKey((T) value);
        return key == null ? null : key.toString();
    }

    public Object func_82594_a(String name) {
        if (name == null) return null;
        ResourceLocation key = ResourceLocation.tryParse(name);
        return key == null ? null : registry.get(key);
    }

    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }
}
