package com.myname.legacyloader.bridge.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.myname.legacyloader.bridge.client.LegacyIcon;
import com.myname.legacyloader.bridge.client.renderer.LegacyTessellator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.level.Level;

import java.util.Random;

public class LegacyEntityFX extends TextureSheetParticle {
    public static double interpPosX;
    public static double interpPosY;
    public static double interpPosZ;
    public static double field_70556_an;
    public static double field_70554_ao;
    public static double field_70555_ap;

    public double posX, posY, posZ;
    public double prevPosX, prevPosY, prevPosZ;
    public double lastTickPosX, lastTickPosY, lastTickPosZ;
    public double motionX, motionY, motionZ;
    public double field_70165_t, field_70163_u, field_70161_v;
    public double field_70169_q, field_70167_r, field_70166_s;
    public double field_70159_w, field_70181_x, field_70179_y;
    public boolean onGround;
    public boolean noClip;
    public boolean isDead;
    public boolean field_70122_E;
    public boolean field_70145_X;
    public float width = 0.2F;
    public float height = 0.2F;
    public float yOffset;

    public int particleTextureIndexX;
    public int particleTextureIndexY;
    public float particleTextureJitterX;
    public float particleTextureJitterY;
    public int particleAge;
    public int particleMaxAge;
    public float particleScale;
    public float particleGravity;
    public float particleRed = 1.0F;
    public float particleGreen = 1.0F;
    public float particleBlue = 1.0F;
    public float particleAlpha = 1.0F;
    public Object particleIcon;
    public int field_94054_b;
    public int field_94055_c;
    public int field_70546_d;
    public int field_70547_e;
    public float field_70544_f;
    public float field_70545_g;
    public float field_70552_h = 1.0F;
    public float field_70553_i = 1.0F;
    public float field_70551_j = 1.0F;
    public float field_82339_as = 1.0F;
    public Object field_70550_a;

    public final Level worldObj;
    public final Level field_70170_p;
    public final Random rand = new Random();
    public final Random field_70146_Z = rand;

    public LegacyEntityFX(Level world, double x, double y, double z) {
        super((ClientLevel) world, x, y, z);
        this.worldObj = world;
        this.field_70170_p = world;
        this.setSize(0.2F, 0.2F);
        this.yOffset = this.height / 2.0F;
        this.setPosition(x, y, z);
        this.lastTickPosX = x;
        this.lastTickPosY = y;
        this.lastTickPosZ = z;
        this.particleTextureJitterX = this.rand.nextFloat() * 3.0F;
        this.particleTextureJitterY = this.rand.nextFloat() * 3.0F;
        this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.particleMaxAge = (int)(4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
        syncNamedFields();
        syncLegacyToModern();
    }

    public LegacyEntityFX(Level world, double x, double y, double z, double vx, double vy, double vz) {
        this(world, x, y, z);
        this.motionX = vx + (Math.random() * 2.0D - 1.0D) * 0.4F;
        this.motionY = vy + (Math.random() * 2.0D - 1.0D) * 0.4F;
        this.motionZ = vz + (Math.random() * 2.0D - 1.0D) * 0.4F;
        double power = (Math.random() + Math.random() + 1.0D) * 0.15F;
        double length = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        if (length > 1.0E-7D) {
            this.motionX = this.motionX / length * power * 0.4D;
            this.motionY = this.motionY / length * power * 0.4D + 0.1D;
            this.motionZ = this.motionZ / length * power * 0.4D;
        }
        syncNamedFields();
        syncLegacyToModern();
    }

    public LegacyEntityFX multiplyVelocity(float multiplier) {
        this.motionX *= multiplier;
        this.motionY = (this.motionY - 0.1D) * multiplier + 0.1D;
        this.motionZ *= multiplier;
        syncNamedFields();
        syncLegacyToModern();
        return this;
    }

    public LegacyEntityFX multipleParticleScaleBy(float scale) {
        this.setSize(0.2F * scale, 0.2F * scale);
        this.particleScale *= scale;
        syncNamedFields();
        syncLegacyToModern();
        return this;
    }

    public void setRBGColorF(float red, float green, float blue) {
        this.particleRed = red;
        this.particleGreen = green;
        this.particleBlue = blue;
        syncNamedFields();
        syncLegacyToModern();
    }

    public void setAlphaF(float alpha) {
        this.particleAlpha = alpha;
        syncNamedFields();
        syncLegacyToModern();
    }

    public void setParticleIcon(Object icon) {
        this.particleIcon = icon;
        this.field_70550_a = icon;
        if (icon instanceof LegacyIcon legacyIcon) {
            LegacyTessellator.setCurrentIcon(legacyIcon);
        }
    }

    public void setParticleTextureIndex(int index) {
        this.particleTextureIndexX = index % 16;
        this.particleTextureIndexY = index / 16;
        syncNamedFields();
    }

    public void nextTextureIndexX() {
        this.particleTextureIndexX++;
        syncNamedFields();
    }

    public int getFXLayer() {
        return 0;
    }

    public void onUpdate() {
        syncAliasToNamed();
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            setDead();
        }
        this.motionY -= 0.04D * this.particleGravity;
        moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
        syncNamedFields();
        syncLegacyToModern();
    }

    @Override
    public void tick() {
        this.onUpdate();
    }

    public void func_70071_h_() {
        this.onUpdate();
    }

    public void moveEntity(double x, double y, double z) {
        super.move(x, y, z);
        this.posX = this.x;
        this.posY = this.y;
        this.posZ = this.z;
        this.onGround = super.onGround;
        syncNamedFields();
    }

    public void func_70091_d(double x, double y, double z) {
        moveEntity(x, y, z);
    }

    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        super.setPos(x, y, z);
        syncNamedFields();
    }

    @Override
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        super.setSize(width, height);
    }

    public void setDead() {
        this.isDead = true;
        super.remove();
    }

    public void func_70106_y() {
        setDead();
    }

    public void func_70105_a(float width, float height) {
        setSize(width, height);
    }

    public void func_70536_a(int index) {
        setParticleTextureIndex(index);
    }

    public void renderParticle(LegacyTessellator tessellator, float partialTicks, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {
    }

    public void func_70539_a(LegacyTessellator tessellator, float partialTicks, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {
        renderParticle(tessellator, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        syncAliasToNamed();
        interpPosX = field_70556_an = camera.getPosition().x;
        interpPosY = field_70554_ao = camera.getPosition().y;
        interpPosZ = field_70555_ap = camera.getPosition().z;
        try {
            this.func_70539_a(LegacyTessellator.instance, partialTicks, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        } catch (Throwable ignored) {
        }
        if (this.sprite != null) {
            super.render(buffer, camera, partialTicks);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private void syncNamedFields() {
        this.field_70165_t = this.posX;
        this.field_70163_u = this.posY;
        this.field_70161_v = this.posZ;
        this.field_70169_q = this.prevPosX;
        this.field_70167_r = this.prevPosY;
        this.field_70166_s = this.prevPosZ;
        this.field_70159_w = this.motionX;
        this.field_70181_x = this.motionY;
        this.field_70179_y = this.motionZ;
        this.field_70122_E = this.onGround;
        this.field_70145_X = this.noClip;
        this.field_94054_b = this.particleTextureIndexX;
        this.field_94055_c = this.particleTextureIndexY;
        this.field_70546_d = this.particleAge;
        this.field_70547_e = this.particleMaxAge;
        this.field_70544_f = this.particleScale;
        this.field_70545_g = this.particleGravity;
        this.field_70552_h = this.particleRed;
        this.field_70553_i = this.particleGreen;
        this.field_70551_j = this.particleBlue;
        this.field_82339_as = this.particleAlpha;
        this.field_70550_a = this.particleIcon;
    }

    private void syncAliasToNamed() {
        this.posX = this.field_70165_t;
        this.posY = this.field_70163_u;
        this.posZ = this.field_70161_v;
        this.prevPosX = this.field_70169_q;
        this.prevPosY = this.field_70167_r;
        this.prevPosZ = this.field_70166_s;
        this.motionX = this.field_70159_w;
        this.motionY = this.field_70181_x;
        this.motionZ = this.field_70179_y;
        this.onGround = this.field_70122_E;
        this.noClip = this.field_70145_X;
        this.particleTextureIndexX = this.field_94054_b;
        this.particleTextureIndexY = this.field_94055_c;
        this.particleAge = this.field_70546_d;
        this.particleMaxAge = this.field_70547_e;
        this.particleScale = this.field_70544_f;
        this.particleGravity = this.field_70545_g;
        this.particleRed = this.field_70552_h;
        this.particleGreen = this.field_70553_i;
        this.particleBlue = this.field_70551_j;
        this.particleAlpha = this.field_82339_as;
        this.particleIcon = this.field_70550_a;
    }

    private void syncLegacyToModern() {
        this.x = this.posX;
        this.y = this.posY;
        this.z = this.posZ;
        this.xo = this.prevPosX;
        this.yo = this.prevPosY;
        this.zo = this.prevPosZ;
        this.xd = this.motionX;
        this.yd = this.motionY;
        this.zd = this.motionZ;
        this.rCol = this.particleRed;
        this.gCol = this.particleGreen;
        this.bCol = this.particleBlue;
        this.alpha = this.particleAlpha;
        this.quadSize = 0.1F * this.particleScale;
        this.gravity = this.particleGravity;
    }
}
