package com.myname.legacyloader.bridge.config;

public class LegacyProperty {
    private String name;
    private String value;
    private String[] values;

    // 1.7.10莠呈鋤縺ｮ縺溘ａ縺ｮpublic繝輔ぅ繝ｼ繝ｫ繝・
    public String comment = "";
    private String languageKey;
    private boolean requiresWorldRestart;
    private boolean requiresMcRestart;
    private boolean showInGui = true;

    public LegacyProperty(String value) {
        this.value = value;
    }

    public LegacyProperty(String name, String value, Type type) {
        this.name = name;
        this.value = value;
    }

    public LegacyProperty(String name, String[] values, Type type) {
        this.name = name;
        this.value = values == null ? "" : String.join(",", values);
        this.values = values == null ? new String[0] : values.clone();
    }

    public LegacyProperty(String value, String comment) {
        this.value = value;
        this.comment = comment;
    }

    public int getInt() {
        return getInt(0);
    }

    public int getInt(int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // 笘・ｿｽ蜉: 蠑墓焚縺ｪ縺・getDouble
    public double getDouble() {
        return getDouble(0.0D);
    }

    public double getDouble(double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean() {
        return getBoolean(false);
    }

    public boolean getBoolean(boolean defaultValue) {
        return Boolean.parseBoolean(value);
    }

    public String getString() {
        return value;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public String[] getStringList() {
        if (values != null) {
            return values.clone();
        }
        if (value == null || value.isEmpty()) {
            return new String[0];
        }
        return value.split("\\s*,\\s*");
    }

    public void set(int val) { this.value = String.valueOf(val); }
    public void set(boolean val) { this.value = String.valueOf(val); }
    public void set(double val) { this.value = String.valueOf(val); }
    public void set(String val) { this.value = val; }

    public LegacyProperty setLanguageKey(String languageKey) {
        this.languageKey = languageKey;
        return this;
    }

    public LegacyProperty setRequiresWorldRestart(boolean requiresWorldRestart) {
        this.requiresWorldRestart = requiresWorldRestart;
        return this;
    }

    public LegacyProperty setRequiresMcRestart(boolean requiresMcRestart) {
        this.requiresMcRestart = requiresMcRestart;
        return this;
    }

    public LegacyProperty setShowInGui(boolean showInGui) {
        this.showInGui = showInGui;
        return this;
    }

    public LegacyProperty setMinValue(int minValue) {
        return this;
    }

    public LegacyProperty setMaxValue(int maxValue) {
        return this;
    }

    public LegacyProperty setMinValue(double minValue) {
        return this;
    }

    public LegacyProperty setMaxValue(double maxValue) {
        return this;
    }

    public enum Type {
        STRING,
        INTEGER,
        BOOLEAN,
        DOUBLE
    }
}
