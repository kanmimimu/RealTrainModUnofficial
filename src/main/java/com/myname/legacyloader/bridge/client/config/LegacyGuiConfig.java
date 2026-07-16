package com.myname.legacyloader.bridge.client.config;

import com.myname.legacyloader.bridge.client.gui.LegacyGuiScreen;

import java.util.ArrayList;
import java.util.List;

public class LegacyGuiConfig extends LegacyGuiScreen {
    public final List<Object> configElements;

    public LegacyGuiConfig(LegacyGuiScreen parentScreen, List<?> configElements, String modID,
                           String configID, boolean allRequireWorldRestart,
                           boolean allRequireMcRestart, String title) {
        this.configElements = new ArrayList<>();
        if (configElements != null) {
            this.configElements.addAll(configElements);
        }
    }
}
