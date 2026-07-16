package com.myname.legacyloader.bridge.config;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LegacyConfiguration {
    public static final String CATEGORY_GENERAL = "general";
    private final Map<String, LegacyConfigCategory> categories = new LinkedHashMap<>();
    //本家 Forge Configuration の getLoadedConfigVersion 用。ファイルは読まないので、
    //渡された定義バージョンをそのまま「読み込んだバージョン」として返す (= 設定は最新扱い)。
    //null を返すと mod によっては設定リセット/移行処理へ進むため、同値を返して正常経路にする。
    private String configVersion;

    public LegacyConfiguration(File file) {
    }

    public LegacyConfiguration(File file, String configVersion) {
        this.configVersion = configVersion;
    }

    public LegacyConfiguration(File file, String configVersion, boolean caseSensitiveCustomCategories) {
        this.configVersion = configVersion;
    }

    /** 本家 Configuration.getLoadedConfigVersion(): 設定ファイルから読んだバージョン文字列。 */
    public String getLoadedConfigVersion() {
        return configVersion;
    }

    /** 本家 Configuration.getDefinedConfigVersion(): コード側が期待するバージョン。 */
    public String getDefinedConfigVersion() {
        return configVersion;
    }

    public void load() {
    }

    public void save() {
    }

    public boolean hasChanged() {
        return false;
    }

    public LegacyProperty get(String category, String key, int defaultValue) {
        return getOrCreate(category, key, new LegacyProperty(String.valueOf(defaultValue)));
    }

    public LegacyProperty get(String category, String key, boolean defaultValue) {
        return getOrCreate(category, key, new LegacyProperty(String.valueOf(defaultValue)));
    }

    public LegacyProperty get(String category, String key, String defaultValue) {
        return getOrCreate(category, key, new LegacyProperty(defaultValue));
    }

    // 笘・ｿｽ蜉: double蟇ｾ蠢・
    public LegacyProperty get(String category, String key, double defaultValue) {
        return getOrCreate(category, key, new LegacyProperty(String.valueOf(defaultValue)));
    }

    // 繧ｳ繝｡繝ｳ繝井ｻ倥″縺ｮ繝舌Μ繧ｨ繝ｼ繧ｷ繝ｧ繝ｳ
    public LegacyProperty get(String category, String key, int defaultValue, String comment) {
        return get(category, key, defaultValue);
    }

    public LegacyProperty get(String category, String key, double defaultValue, String comment) {
        return get(category, key, defaultValue);
    }

    public LegacyProperty get(String category, String key, String defaultValue, String comment) {
        return get(category, key, defaultValue);
    }

    public LegacyProperty get(String category, String key, String[] defaultValue, String comment) {
        return getOrCreate(category, key, new LegacyProperty(key, defaultValue, LegacyProperty.Type.STRING));
    }

    // 本家 Forge Configuration の便利ゲッター群 (値を直接返す)。comment 版 / comment+langKey 版 /
    // validValues 版まで網羅する。ファイル I/O はしないスタブなので既定値 (int/float は範囲クランプ) を返す。
    public int getInt(String key, String category, int defaultValue, int minValue, int maxValue, String comment) {
        return Math.max(minValue, Math.min(maxValue, defaultValue));
    }

    public int getInt(String key, String category, int defaultValue, int minValue, int maxValue, String comment, String langKey) {
        return getInt(key, category, defaultValue, minValue, maxValue, comment);
    }

    public boolean getBoolean(String key, String category, boolean defaultValue, String comment) {
        return defaultValue;
    }

    public boolean getBoolean(String key, String category, boolean defaultValue, String comment, String langKey) {
        return defaultValue;
    }

    public float getFloat(String key, String category, float defaultValue, float minValue, float maxValue, String comment) {
        return Math.max(minValue, Math.min(maxValue, defaultValue));
    }

    public float getFloat(String key, String category, float defaultValue, float minValue, float maxValue, String comment, String langKey) {
        return getFloat(key, category, defaultValue, minValue, maxValue, comment);
    }

    public String getString(String key, String category, String defaultValue, String comment) {
        return defaultValue;
    }

    public String getString(String key, String category, String defaultValue, String comment, String langKey) {
        return defaultValue;
    }

    public String getString(String key, String category, String defaultValue, String comment, String[] validValues, String langKey) {
        return defaultValue;
    }

    public String[] getStringList(String key, String category, String[] defaultValues, String comment) {
        return defaultValues;
    }

    public String[] getStringList(String key, String category, String[] defaultValues, String comment, String[] validValues) {
        return defaultValues;
    }

    public String[] getStringList(String key, String category, String[] defaultValues, String comment, String[] validValues, String langKey) {
        return defaultValues;
    }

    public void addCustomCategoryComment(String category, String comment) {
    }

    public LegacyConfigCategory getCategory(String category) {
        return categories.computeIfAbsent(category, LegacyConfigCategory::new);
    }

    public Set<String> getCategoryNames() {
        return categories.keySet();
    }

    public LegacyConfiguration setCategoryLanguageKey(String category, String langKey) {
        return this;
    }

    public LegacyConfiguration setCategoryRequiresMcRestart(String category, boolean requiresMcRestart) {
        return this;
    }

    private LegacyProperty getOrCreate(String category, String key, LegacyProperty defaultProperty) {
        LegacyConfigCategory configCategory = getCategory(category);
        LegacyProperty existing = configCategory.get(key);
        if (existing != null) {
            return existing;
        }
        configCategory.put(key, defaultProperty);
        return defaultProperty;
    }
}
