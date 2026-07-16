package com.myname.legacyloader.bridge.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LegacyEntityArrow extends Arrow {
    public double field_70165_t;
    public double field_70163_u;
    public double field_70161_v;
    public double field_70159_w;
    public double field_70181_x;
    public double field_70179_y;
    public float field_70177_z;
    public float field_70125_A;
    public boolean field_70128_L;
    public Level field_70170_p;

    public LegacyEntityArrow(Level level) {
        super(level, 0.0, 0.0, 0.0, new ItemStack(Items.ARROW), null);
        this.field_70170_p = level;
    }

    public LegacyEntityArrow(Level level, double x, double y, double z) {
        super(level, x, y, z, new ItemStack(Items.ARROW), null);
        this.field_70170_p = level;
        setLegacyPosition(x, y, z);
    }

    public LegacyEntityArrow(Level level, LivingEntity shooter, float velocity) {
        super(level, shooter, new ItemStack(Items.ARROW), null);
        this.field_70170_p = level;
        if (velocity > 0.0F) {
            setDeltaMovement(getDeltaMovement().scale(velocity));
        }
        setLegacyPosition(getX(), getY(), getZ());
    }

    public void func_70107_b(double x, double y, double z) {
        setLegacyPosition(x, y, z);
    }

    public void setPosition(double x, double y, double z) {
        setLegacyPosition(x, y, z);
    }

    public void func_70239_b(double damage) {
        setBaseDamage(damage);
    }

    public void func_70240_a(int pickupMode) {
        this.pickup = pickupMode == 1 ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
    }

    public void setDead() {
        this.field_70128_L = true;
        discard();
    }

    public boolean isEntityAlive() {
        return !field_70128_L && !isRemoved();
    }

    public void syncLegacyState() {
        setPos(field_70165_t, field_70163_u, field_70161_v);
        setDeltaMovement(new Vec3(field_70159_w, field_70181_x, field_70179_y));
        setYRot(field_70177_z);
        setXRot(field_70125_A);
    }

    private void setLegacyPosition(double x, double y, double z) {
        this.field_70165_t = x;
        this.field_70163_u = y;
        this.field_70161_v = z;
        setPos(x, y, z);
    }
}
