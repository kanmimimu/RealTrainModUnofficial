package com.myname.legacyloader.bridge.config;

public class LegacyConfigChangedEvent {
    public static class OnConfigChangedEvent {
        public final String modID;

        public OnConfigChangedEvent(String modID, String configID, boolean isWorldRunning, boolean requiresMcRestart) {
            this.modID = modID;
        }
    }

    public static class PostConfigChangedEvent extends OnConfigChangedEvent {
        public PostConfigChangedEvent(String modID, String configID, boolean isWorldRunning, boolean requiresMcRestart) {
            super(modID, configID, isWorldRunning, requiresMcRestart);
        }
    }
}
