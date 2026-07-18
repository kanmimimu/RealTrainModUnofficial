package com.myname.legacyloader.bridge.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;

public class LegacyPreInitEvent {
    public Logger getModLog() {
        return LogManager.getLogger("LegacyMod");
    }

    public File getSuggestedConfigurationFile() {
        return new File("config/legacymod.cfg");
    }

    public File getModConfigurationDirectory() {
        return new File("config");
    }
}
