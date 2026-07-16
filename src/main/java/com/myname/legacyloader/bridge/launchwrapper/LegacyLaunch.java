package com.myname.legacyloader.bridge.launchwrapper;

import com.myname.legacyloader.core.LegacyClassLoader;
import java.util.HashMap;
import java.util.Map;

public class LegacyLaunch {
    // 1.7.10: public static LaunchClassLoader classLoader;
    public static LegacyClassLoader classLoader;

    // 1.7.10: public static Map<String,Object> blackboard;
    public static Map<String, Object> blackboard = new HashMap<>();

    public static void init(LegacyClassLoader loader) {
        classLoader = loader;
    }
}