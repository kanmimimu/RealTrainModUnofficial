package com.myname.legacyloader.bridge.client.model;

public class LegacyModelRenderer {
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public boolean showModel = true;
    public boolean isHidden = false;
    public boolean mirror = false;
    public float textureWidth = 64.0F;
    public float textureHeight = 32.0F;
    public int textureOffsetX;
    public int textureOffsetY;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float field_78795_f;
    public float field_78796_g;
    public float field_78808_h;
    public float field_78800_c;
    public float field_78797_d;
    public float field_78798_e;
    public float field_78801_a = 64.0F;
    public float field_78799_b = 32.0F;
    public boolean field_78809_i = false;
    public boolean field_78807_k = true;
    public boolean field_78806_j = false;

    // 繧ｳ繝ｳ繧ｹ繝医Λ繧ｯ繧ｿ
    public LegacyModelRenderer(LegacyModelBase model, String boxName) { }
    public LegacyModelRenderer(LegacyModelBase model) { }
    public LegacyModelRenderer(LegacyModelBase model, int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
    }

    // 繝懊ャ繧ｯ繧ｹ霑ｽ蜉
    public void addBox(float x, float y, float z, int width, int height, int depth) {}
    public void addBox(String partName, float x, float y, float z, int width, int height, int depth) {}
    public void addBox(float x, float y, float z, int width, int height, int depth, float scale) {} // 霑ｽ蜉

    // 菴咲ｽｮ險ｭ螳・
    public void setRotationPoint(float x, float y, float z) {
        this.rotationPointX = x;
        this.rotationPointY = y;
        this.rotationPointZ = z;
        this.field_78800_c = x;
        this.field_78797_d = y;
        this.field_78798_e = z;
    }

    // 謠冗判繝｡繧ｽ繝・ラ
    public void render(float scale) {
        syncAliasesToNamed();
        // 1.7.10縺ｧ縺ｯ縺薙％縺ｧGL11繧剃ｽｿ縺｣縺ｦ謠冗判縺励∪縺吶′縲・
        // LegacyLoader縺ｧ縺ｯ謠冗判繧偵お繝溘Η繝ｬ繝ｼ繝医☆繧九°辟｡隕悶＠縺ｾ縺吶・
    }

    // 繝・け繧ｹ繝√Ε險ｭ螳・
    public LegacyModelRenderer setTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public LegacyModelRenderer setTextureSize(int w, int h) {
        this.textureWidth = w;
        this.textureHeight = h;
        this.field_78801_a = w;
        this.field_78799_b = h;
        return this;
    }

    // SRG蜷阪お繧､繝ｪ繧｢繧ｹ
    public void func_78785_a(float scale) {
        render(scale);
    }

    public LegacyModelRenderer func_78789_a(float x, float y, float z, int width, int height, int depth) {
        addBox(x, y, z, width, height, depth);
        return this;
    }

    public LegacyModelRenderer func_78790_a(float x, float y, float z, int width, int height, int depth, float scale) {
        addBox(x, y, z, width, height, depth, scale);
        return this;
    }

    public void func_78793_a(float x, float y, float z) {
        setRotationPoint(x, y, z);
    }

    public LegacyModelRenderer func_78787_b(int width, int height) {
        return setTextureSize(width, height);
    }

    public LegacyModelRenderer func_78784_a(int x, int y) {
        return setTextureOffset(x, y);
    }

    private void syncAliasesToNamed() {
        this.rotateAngleX = this.field_78795_f;
        this.rotateAngleY = this.field_78796_g;
        this.rotateAngleZ = this.field_78808_h;
        this.rotationPointX = this.field_78800_c;
        this.rotationPointY = this.field_78797_d;
        this.rotationPointZ = this.field_78798_e;
        this.textureWidth = this.field_78801_a;
        this.textureHeight = this.field_78799_b;
        this.mirror = this.field_78809_i;
        this.showModel = this.field_78807_k;
        this.isHidden = this.field_78806_j;
    }
}
