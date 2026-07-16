package com.myname.legacyloader.bridge.fml;

import org.apache.logging.log4j.Logger;

public final class LegacyLogHelper {
    private LegacyLogHelper() {
    }

    public static void log(Logger logger, LegacyLogLevel level, String message) {
        if (logger == null) return;
        String name = level == null ? "INFO" : level.toString();
        switch (name) {
            case "ERROR" -> logger.error(message);
            case "WARN" -> logger.warn(message);
            case "DEBUG" -> logger.debug(message);
            case "TRACE" -> logger.trace(message);
            default -> logger.info(message);
        }
    }
}
