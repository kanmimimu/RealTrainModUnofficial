package com.myname.legacyloader.bridge.client.model;

public class LegacyModelBiped extends LegacyModelBase {
    public LegacyModelRenderer bipedHead;
    public LegacyModelRenderer bipedHeadwear;
    public LegacyModelRenderer bipedBody;
    public LegacyModelRenderer bipedRightArm;
    public LegacyModelRenderer bipedLeftArm;
    public LegacyModelRenderer bipedRightLeg;
    public LegacyModelRenderer bipedLeftLeg;
    public LegacyModelRenderer bipedEars;
    public LegacyModelRenderer bipedCloak;

    public int heldItemLeft;
    public int heldItemRight;
    public boolean isSneak;
    public boolean aimedBow;

    public LegacyModelBiped() {
        this(0.0F);
    }

    public LegacyModelBiped(float modelSize) {
        this(modelSize, 0.0F, 64, 32);
    }

    public LegacyModelBiped(float modelSize, float p_i1149_2_, int textureWidthIn, int textureHeightIn) {
        // 繝繝溘・縺ｮ蛻晄悄蛹・(NullPointerException蝗樣∩縺ｮ縺溘ａ)
        this.bipedHead = new LegacyModelRenderer(this, 0, 0);
        this.bipedHeadwear = new LegacyModelRenderer(this, 32, 0);
        this.bipedBody = new LegacyModelRenderer(this, 16, 16);
        this.bipedRightArm = new LegacyModelRenderer(this, 40, 16);
        this.bipedLeftArm = new LegacyModelRenderer(this, 40, 16);
        this.bipedRightLeg = new LegacyModelRenderer(this, 0, 16);
        this.bipedLeftLeg = new LegacyModelRenderer(this, 0, 16);
    }
}