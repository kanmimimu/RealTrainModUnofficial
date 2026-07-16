package com.myname.legacyloader.bridge.fml;

public class LegacyLogLevel {
    public static final LegacyLogLevel ALL     = new LegacyLogLevel("ALL");
    public static final LegacyLogLevel OFF     = new LegacyLogLevel("OFF");
    public static final LegacyLogLevel ERROR   = new LegacyLogLevel("ERROR");
    public static final LegacyLogLevel SEVERE  = new LegacyLogLevel("SEVERE");
    public static final LegacyLogLevel FATAL   = new LegacyLogLevel("FATAL");
    public static final LegacyLogLevel INFO    = new LegacyLogLevel("INFO");
    public static final LegacyLogLevel WARN    = new LegacyLogLevel("WARN");
    public static final LegacyLogLevel WARNING = new LegacyLogLevel("WARNING");
    public static final LegacyLogLevel CONFIG  = new LegacyLogLevel("CONFIG");
    public static final LegacyLogLevel DEBUG   = new LegacyLogLevel("DEBUG");
    public static final LegacyLogLevel FINE    = new LegacyLogLevel("FINE");
    public static final LegacyLogLevel FINER   = new LegacyLogLevel("FINER");
    public static final LegacyLogLevel FINEST  = new LegacyLogLevel("FINEST");
    public static final LegacyLogLevel TRACE   = new LegacyLogLevel("TRACE");

    private final String name;

    public LegacyLogLevel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
