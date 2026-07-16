package com.myname.legacyloader.bridge.fml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyFMLLog {
    private static final Logger LOGGER = LogManager.getLogger("legacy_mod");

    public static void log(LegacyLogLevel level, String format, Object... data) {
        String msg = "[Legacy] " + String.format(format.replace("%s", "%s"), data);
        if (level == LegacyLogLevel.ERROR) LOGGER.error(msg);
        else if (level == LegacyLogLevel.WARN) LOGGER.warn(msg);
        else LOGGER.info(msg);
    }

    public static void log(LegacyLogLevel level, Throwable ex, String format, Object... data) {
        String msg = "[Legacy] " + format;
        if (level == LegacyLogLevel.ERROR) LOGGER.error(msg, ex);
        else LOGGER.warn(msg, ex);
    }

    public static void info(String format, Object... data) {
        log(LegacyLogLevel.INFO, format, data);
    }

    public static void warning(String format, Object... data) {
        log(LegacyLogLevel.WARN, format, data);
    }

    public static void severe(String format, Object... data) {
        log(LegacyLogLevel.ERROR, format, data);
    }
}
