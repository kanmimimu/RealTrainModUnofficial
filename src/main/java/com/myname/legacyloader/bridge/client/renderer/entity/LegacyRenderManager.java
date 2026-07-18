package com.myname.legacyloader.bridge.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public class LegacyRenderManager {
    // 1.7.10: RenderManager.instance
    public static final LegacyRenderManager instance = new LegacyRenderManager();
    public static final LegacyRenderManager field_78727_a = instance;

    public static double renderPosX;
    public static double renderPosY;
    public static double renderPosZ;
    public static double field_78725_b;
    public static double field_78726_c;
    public static double field_78723_d;

    public Object entityRenderMap;
    public Object field_78729_o;
    public Object renderEngine;
    public Object field_78724_e;
    public Object itemRenderer;
    public Object field_78721_f;
    public Object worldObj;
    public Object field_78722_g;
    public Object livingPlayer;
    public Object field_78734_h;
    public Object field_147941_i;
    public float playerViewY;
    public float field_78735_i;
    public float playerViewX;
    public float field_78732_j;
    public Object options;
    public Object field_78733_k;
    public double viewerPosX;
    public double field_78730_l;
    public double viewerPosY;
    public double field_78731_m;
    public double viewerPosZ;
    public double field_78728_n;
    public static boolean debugBoundingBox;
    public static boolean field_85095_o;

    // 螳滉ｽ薙・繝舌ル繝ｩ縺ｮ Dispatcher
    public EntityRenderDispatcher getDispatcher() {
        return Minecraft.getInstance().getEntityRenderDispatcher();
    }
}
