package com.myname.legacyloader.bridge.fml;

import com.myname.legacyloader.bridge.client.gui.LegacyGuiScreen;
import net.minecraft.client.gui.Font;

public abstract class LegacyCustomModLoadingErrorDisplayException extends LegacyEnhancedRuntimeException implements LegacyIFMLHandledException {
    public LegacyCustomModLoadingErrorDisplayException() {
    }

    public LegacyCustomModLoadingErrorDisplayException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract void initGui(LegacyGuiScreen screen, Font font);

    public abstract void drawScreen(LegacyGuiScreen screen, Font font, int mouseX, int mouseY, float partialTicks);

    @Override
    public void printStackTrace(WrappedPrintStream stream) {
    }
}
