package com.myname.legacyloader.bridge.util;

import java.util.Arrays;

public class LegacyChatComponentTranslation implements LegacyIChatComponent {
    private final String key;
    private final Object[] args;
    private LegacyChatStyle style;

    public LegacyChatComponentTranslation(String key, Object... args) {
        this.key = key == null ? "" : key;
        this.args = args == null ? new Object[0] : args;
    }

    @Override
    public LegacyIChatComponent func_150255_a(LegacyChatStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public String getUnformattedText() {
        if (args.length == 0) {
            return key;
        }
        return key + " " + Arrays.toString(args);
    }

    @Override
    public String toString() {
        return (style == null ? "" : style.toString()) + getUnformattedText();
    }
}
