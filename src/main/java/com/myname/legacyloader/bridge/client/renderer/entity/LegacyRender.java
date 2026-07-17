package com.myname.legacyloader.bridge.client.renderer.entity;

import com.myname.legacyloader.bridge.client.renderer.LegacyRenderBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// 1.7.10縺ｮ Render 繧ｯ繝ｩ繧ｹ縺ｮ莉｣繧上ｊ
public abstract class LegacyRender extends EntityRenderer<Entity> {
    public LegacyRenderBlocks field_147909_c = new LegacyRenderBlocks();
    public float shadowSize;
    public float field_76989_e;

    // 1.7.10 Mod縺梧悄蠕・☆繧九悟ｼ墓焚縺ｪ縺励さ繝ｳ繧ｹ繝医Λ繧ｯ繧ｿ縲・
    public LegacyRender() {
        // 1.20.1縺ｮ隕ｪ繧ｯ繝ｩ繧ｹ縺ｯ Context 繧定ｦ∵ｱゅ☆繧九◆繧√｀inecraft繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ縺九ｉ蜿門ｾ励＠縺ｦ貂｡縺・
        super(createContext());
    }

    private static EntityRendererProvider.Context createContext() {
        return new EntityRendererProvider.Context(
                Minecraft.getInstance().getEntityRenderDispatcher(),
                Minecraft.getInstance().getItemRenderer(),
                Minecraft.getInstance().getBlockRenderer(),
                Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer(),
                Minecraft.getInstance().getResourceManager(),
                Minecraft.getInstance().getEntityModels(),
                Minecraft.getInstance().font
        );
    }

    // 1.7.10: getEntityTexture(Entity)
    // 1.20.1: getTextureLocation(T)
    // Mod蛛ｴ縺ｯ縺薙・繝｡繧ｽ繝・ラ繧偵携etEntityTexture縲阪→縺励※繧ｪ繝ｼ繝舌・繝ｩ繧､繝峨＠縺ｦ縺・ｋ蜿ｯ閭ｽ諤ｧ縺後≠繧翫∪縺吶′縲・
    // 繝槭ャ繝斐Φ繧ｰ縺ｧ蜷榊燕繧貞粋繧上○繧九°縲√％縺薙〒蜷ｸ蜿弱＠縺ｾ縺吶・
    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return getEntityTexture(entity);
    }

    // Mod縺悟ｮ溯｣・☆縺ｹ縺肴歓雎｡繝｡繧ｽ繝・ラ
    protected abstract ResourceLocation getEntityTexture(Entity entity);

    // SRG蜷阪お繧､繝ｪ繧｢繧ｹ (func_110775_a -> getTextureLocation)
    public ResourceLocation func_110775_a(Entity entity) {
        return getTextureLocation(entity);
    }

    public void bindTexture(ResourceLocation location) {
    }

    public void func_110776_a(ResourceLocation location) {
        bindTexture(location);
    }
}
