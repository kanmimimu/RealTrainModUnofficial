package com.myname.legacyloader.bridge.client.renderer.entity;

import com.myname.legacyloader.bridge.entity.LegacyEntityItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LegacyRenderItem {
    public float zLevel;
    public float field_77023_b;
    protected LegacyRenderManager renderManager;
    protected LegacyRenderManager field_76990_c;

    public LegacyRenderItem() {
    }

    public void setRenderManager(LegacyRenderManager manager) {
        this.renderManager = manager;
        this.field_76990_c = manager;
    }

    public void func_76976_a(LegacyRenderManager manager) {
        setRenderManager(manager);
    }

    public boolean shouldBob() {
        return true;
    }

    public boolean shouldSpreadItems() {
        return true;
    }

    public void doRender(LegacyEntityItem entity, double x, double y, double z, float yaw, float partialTicks) {
        renderStack(entity != null ? entity.getItem() : ItemStack.EMPTY, ItemDisplayContext.GROUND);
    }

    public void func_76986_a(LegacyEntityItem entity, double x, double y, double z, float yaw, float partialTicks) {
        doRender(entity, x, y, z, yaw, partialTicks);
    }

    public void func_76986_a(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        if (entity instanceof LegacyEntityItem item) {
            doRender(item, x, y, z, yaw, partialTicks);
        }
    }

    public void renderItemAndEffectIntoGUI(Font font, TextureManager textureManager, ItemStack stack, int x, int y) {
        renderStack(stack, ItemDisplayContext.GUI);
    }

    public void func_82406_b(Font font, TextureManager textureManager, ItemStack stack, int x, int y) {
        renderItemAndEffectIntoGUI(font, textureManager, stack, x, y);
    }

    public void renderItemOverlayIntoGUI(Font font, TextureManager textureManager, ItemStack stack, int x, int y) {
    }

    public void func_77021_b(Font font, TextureManager textureManager, ItemStack stack, int x, int y) {
        renderItemOverlayIntoGUI(font, textureManager, stack, x, y);
    }

    protected ResourceLocation getEntityTexture(LegacyEntityItem entity) {
        return ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    }

    protected ResourceLocation func_110775_a(LegacyEntityItem entity) {
        return getEntityTexture(entity);
    }

    private void renderStack(ItemStack stack, ItemDisplayContext context) {
        if (stack == null || stack.isEmpty()) return;
        try {
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, 0xF000F0, 0, null, null, null, 0);
        } catch (Throwable ignored) {
        }
    }
}
