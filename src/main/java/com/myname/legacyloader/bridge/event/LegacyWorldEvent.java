package com.myname.legacyloader.bridge.event;

import com.myname.legacyloader.bridge.fml.LegacyEvent;
import net.minecraft.world.level.Level;

public class LegacyWorldEvent extends LegacyEvent {
    public final Level world;

    public LegacyWorldEvent() {
        this(null);
    }

    public LegacyWorldEvent(Level world) {
        this.world = world;
    }

    public Level getWorld() {
        return world;
    }

    public static class Unload extends LegacyWorldEvent {
        public Unload() {
            super();
        }

        public Unload(Level world) {
            super(world);
        }
    }
}
