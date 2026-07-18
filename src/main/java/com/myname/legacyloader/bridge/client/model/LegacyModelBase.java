package com.myname.legacyloader.bridge.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;

public abstract class LegacyModelBase extends Model {

    public int textureWidth = 64;
    public int textureHeight = 32;

    // 笘・㍾隕・ public 縺ｧ螳夂ｾｩ縺吶ｋ縺薙→
    public boolean isChild = false;
    public boolean field_78098_b = false; // isChild 縺ｮSRG蜷・

    public boolean isRiding = false;
    public boolean field_78090_t = false; // isRiding 縺ｮSRG蜷・

    public float onGround;
    public float field_78040_i = 0.0F; // onGround 縺ｮSRG蜷・

    public LegacyModelBase() {
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        // 蛟､縺ｮ蜷梧悄
        if (this.field_78098_b) this.isChild = true;
        if (this.field_78090_t) this.isRiding = true;
        if (this.field_78040_i != 0) this.onGround = this.field_78040_i;
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {}

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        render(entity, f, f1, f2, f3, f4, f5);
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {}

    public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }
}
