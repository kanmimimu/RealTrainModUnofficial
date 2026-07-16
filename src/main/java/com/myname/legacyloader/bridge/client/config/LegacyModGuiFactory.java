package com.myname.legacyloader.bridge.client.config;

import com.myname.legacyloader.bridge.client.gui.LegacyGuiScreen;
import net.minecraft.client.Minecraft;

import java.util.Set;

public interface LegacyModGuiFactory {
    default void initialize(Minecraft minecraftInstance) {
    }

    default Class<? extends LegacyGuiScreen> mainConfigGuiClass() {
        return null;
    }

    default Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    default RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    class RuntimeOptionCategoryElement {
    }

    interface RuntimeOptionGuiHandler {
    }
}
