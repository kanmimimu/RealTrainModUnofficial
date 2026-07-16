package com.myname.legacyloader.bridge.util;

public enum LegacyEnumChatFormatting {
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    OBFUSCATED('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r');

    private final String code;

    LegacyEnumChatFormatting(char code) {
        this.code = "\u00a7" + code;
    }

    public static String func_110646_a(String value) {
        return value == null ? null : value.replaceAll("(?i)\u00a7[0-9A-FK-OR]", "");
    }

    @Override
    public String toString() {
        return code;
    }
}
