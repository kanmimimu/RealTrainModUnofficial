package com.myname.legacyloader.bridge.util;

public class LegacyChatStyle {
    private LegacyEnumChatFormatting color;
    private Boolean bold;

    public LegacyChatStyle func_150238_a(LegacyEnumChatFormatting color) {
        this.color = color;
        return this;
    }

    public LegacyChatStyle func_150227_a(Boolean bold) {
        this.bold = bold;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (color != null) {
            builder.append(color);
        }
        if (Boolean.TRUE.equals(bold)) {
            builder.append(LegacyEnumChatFormatting.BOLD);
        }
        return builder.toString();
    }
}
