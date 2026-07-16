package com.myname.legacyloader.bridge.world.biome;

public class LegacyBiomeGenBase {
    public static final LegacyBiomeGenBase[] field_76773_a = new LegacyBiomeGenBase[256];
    public static final Height field_150595_c = new Height(-1.0F, 0.1F);

    public static final LegacyBiomeGenBase field_76768_g = new LegacyBiomeGenDesert(2).func_76735_a("Desert");
    public static final LegacyBiomeGenBase field_76779_k = new LegacyBiomeGenOcean(10).func_76735_a("FrozenOcean");
    public static final LegacyBiomeGenBase field_76778_j = new LegacyBiomeGenOcean(0).func_76735_a("Ocean");

    public final int field_76756_M;
    public String field_76791_y;
    public int field_76790_z;
    public float field_76748_D;
    public float field_76749_E;
    public Height field_150609_ah = new Height(0.1F, 0.2F);

    public LegacyBiomeGenBase(int id) {
        this.field_76756_M = id;
        this.field_76791_y = "Biome " + id;
        if (id >= 0 && id < field_76773_a.length) {
            field_76773_a[id] = this;
        }
    }

    public static LegacyBiomeGenBase[] func_150565_n() {
        return field_76773_a;
    }

    public LegacyBiomeGenBase func_76739_b(int color) {
        this.field_76790_z = color;
        return this;
    }

    public LegacyBiomeGenBase func_76735_a(String name) {
        this.field_76791_y = name;
        return this;
    }

    public LegacyBiomeGenBase func_76745_m() {
        return this;
    }

    public LegacyBiomeGenBase func_76732_a(float temperature, float rainfall) {
        this.field_76748_D = temperature;
        this.field_76749_E = rainfall;
        return this;
    }

    public LegacyBiomeGenBase func_150570_a(Height height) {
        this.field_150609_ah = height;
        return this;
    }

    public static class Height {
        public final float field_150775_c;
        public final float field_150772_d;

        public Height(float rootHeight, float heightVariation) {
            this.field_150775_c = rootHeight;
            this.field_150772_d = heightVariation;
        }
    }
}
