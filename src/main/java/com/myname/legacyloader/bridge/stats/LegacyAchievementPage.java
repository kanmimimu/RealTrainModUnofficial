package com.myname.legacyloader.bridge.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LegacyAchievementPage {
    private final String name;
    private final List<LegacyAchievement> achievements;

    public LegacyAchievementPage(String name, LegacyAchievement[] achievements) {
        this.name = name;
        this.achievements = new ArrayList<>();
        if (achievements != null) {
            this.achievements.addAll(Arrays.asList(achievements));
        }
    }

    public static void registerAchievementPage(LegacyAchievementPage page) {}

    public List<LegacyAchievement> getAchievements() {
        return achievements;
    }

    public String getName() {
        return name;
    }
}
