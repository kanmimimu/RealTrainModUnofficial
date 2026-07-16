package com.myname.legacyloader.bridge.client;

import com.myname.legacyloader.bridge.client.renderer.LegacyTessellator;

public interface LegacyIcon {
    int getIconWidth();
    int getIconHeight();
    float getMinU();
    float getMaxU();
    float getMinV();
    float getMaxV();
    String getIconName();

    default float getInterpolatedU(double u) {
        LegacyTessellator.setCurrentIcon(this);
        return getMinU() + (getMaxU() - getMinU()) * (float)(u / 16.0D);
    }

    default float getInterpolatedV(double v) {
        LegacyTessellator.setCurrentIcon(this);
        return getMinV() + (getMaxV() - getMinV()) * (float)(v / 16.0D);
    }

    // 笘・ｿｽ蜉: SRG蜷阪お繧､繝ｪ繧｢繧ｹ
    default int func_94211_a() { return getIconWidth(); }
    default int func_94212_b() { return getIconHeight(); }
    default float func_94209_e() { LegacyTessellator.setCurrentIcon(this); return getMinU(); }
    default float func_94212_f() { LegacyTessellator.setCurrentIcon(this); return getMaxU(); }
    default float func_94206_g() { LegacyTessellator.setCurrentIcon(this); return getMinV(); }
    default float func_94210_h() { LegacyTessellator.setCurrentIcon(this); return getMaxV(); }
    default String func_94215_i() { return getIconName(); }
    default float func_94214_a(double u) { return getInterpolatedU(u); }
    default float func_94207_b(double v) { return getInterpolatedV(v); }
}
