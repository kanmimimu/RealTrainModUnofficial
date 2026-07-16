package com.myname.legacyloader.bridge.event;

import com.myname.legacyloader.bridge.registry.BridgeRegistry;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.Event;

public class BridgeRegistryEvent extends Event {
    public static class Register<T> extends Event {
        private final BridgeRegistry<T> registry;

        public Register(Registry<T> realRegistry) {
            this.registry = new BridgeRegistry<>(realRegistry);
        }

        public BridgeRegistry<T> getRegistry() {
            return registry;
        }
    }
}
