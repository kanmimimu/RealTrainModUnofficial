package com.myname.legacyloader.bridge.client.event;

import com.myname.legacyloader.bridge.client.LegacyTextureMap;
import com.myname.legacyloader.bridge.fml.LegacyEvent;

public class LegacyTextureStitchEvent extends LegacyEvent {
    public final LegacyTextureMap map;

    public LegacyTextureStitchEvent(LegacyTextureMap map) {
        this.map = map == null ? new LegacyTextureMap() : map;
    }

    public static class Pre extends LegacyTextureStitchEvent {
        public Pre() {
            this(new LegacyTextureMap());
        }

        public Pre(LegacyTextureMap map) {
            super(map);
        }
    }

    public static class Post extends LegacyTextureStitchEvent {
        public Post() {
            this(new LegacyTextureMap());
        }

        public Post(LegacyTextureMap map) {
            super(map);
        }
    }
}
