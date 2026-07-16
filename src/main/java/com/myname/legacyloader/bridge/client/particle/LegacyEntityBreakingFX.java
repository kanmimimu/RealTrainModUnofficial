package com.myname.legacyloader.bridge.client.particle;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class LegacyEntityBreakingFX extends LegacyEntityFX {
    public LegacyEntityBreakingFX(Level world, double x, double y, double z, Item item) {
        this(world, x, y, z, item, 0);
    }

    public LegacyEntityBreakingFX(Level world, double x, double y, double z, Item item, int damage) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleGravity = 1.0F;
        this.particleScale *= 0.5F;
        this.field_70552_h = this.field_70553_i = this.field_70551_j = 1.0F;
        this.field_70545_g = this.particleGravity;
        this.field_70544_f = this.particleScale;
    }

    public LegacyEntityBreakingFX(Level world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Item item, int damage) {
        this(world, x, y, z, item, damage);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += xSpeed;
        this.motionY += ySpeed;
        this.motionZ += zSpeed;
        this.field_70159_w = this.motionX;
        this.field_70181_x = this.motionY;
        this.field_70179_y = this.motionZ;
    }

    @Override
    public int getFXLayer() {
        return 2;
    }
}
