package com.myname.legacyloader.bridge.util;

public class LegacyChatComponentText implements LegacyIChatComponent {
    private final String text;
    private LegacyChatStyle style;

    public LegacyChatComponentText(String text) {
        this.text = text == null ? "" : text;
    }

    @Override
    public LegacyIChatComponent func_150255_a(LegacyChatStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public String getUnformattedText() {
        return text;
    }

    @Override
    public String toString() {
        return (style == null ? "" : style.toString()) + text;
    }
}
