package com.myname.legacyloader.bridge.common;

public class LegacyToolType {
    private final String name;

    public LegacyToolType(String name) {
        this.name = name;
    }

    // 1.16.5縺ｮ荳ｻ隕√↑繝・・繝ｫ繧ｿ繧､繝・
    public static final LegacyToolType AXE = new LegacyToolType("axe");
    public static final LegacyToolType PICKAXE = new LegacyToolType("pickaxe");
    public static final LegacyToolType SHOVEL = new LegacyToolType("shovel");
    public static final LegacyToolType HOE = new LegacyToolType("hoe");

    // 譁・ｭ怜・縺九ｉ蜿門ｾ励☆繧九Γ繧ｽ繝・ラ
    public static LegacyToolType get(String name) {
        switch (name) {
            case "axe": return AXE;
            case "pickaxe": return PICKAXE;
            case "shovel": return SHOVEL;
            case "hoe": return HOE;
            default: return new LegacyToolType(name);
        }
    }

    public String getName() {
        return name;
    }
}