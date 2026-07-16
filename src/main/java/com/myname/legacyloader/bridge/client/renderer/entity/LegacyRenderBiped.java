package com.myname.legacyloader.bridge.client.renderer.entity;

import com.myname.legacyloader.bridge.client.model.LegacyModelBiped;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class LegacyRenderBiped extends LegacyRender {

    public LegacyModelBiped modelBipedMain;

    // 1.7.10: RenderBiped(ModelBiped model, float shadowSize)
    public LegacyRenderBiped(LegacyModelBiped model, float shadowSize) {
        super(); // LegacyRender縺ｮ繧ｳ繝ｳ繧ｹ繝医Λ繧ｯ繧ｿ(Context險ｭ螳・繧貞他縺ｶ
        this.modelBipedMain = model;
        this.shadowRadius = shadowSize;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        // Mod縺後が繝ｼ繝舌・繝ｩ繧､繝峨＠縺ｦ縺・↑縺・ｴ蜷医・繝輔か繝ｼ繝ｫ繝舌ャ繧ｯ
        return ResourceLocation.fromNamespaceAndPath("legacyloader", "textures/missing.png");
    }
}