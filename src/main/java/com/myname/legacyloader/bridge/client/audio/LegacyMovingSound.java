package com.myname.legacyloader.bridge.client.audio;

import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;

public abstract class LegacyMovingSound implements LegacyISound, TickableSoundInstance {
    protected ResourceLocation field_147664_a;
    protected float field_147662_b = 1.0F;
    protected float field_147663_c = 1.0F;
    protected float field_147660_d;
    protected float field_147661_e;
    protected float field_147658_f;
    protected boolean field_147659_g;
    protected int field_147665_h;
    protected AttenuationType field_147666_i = AttenuationType.LINEAR;
    protected boolean field_147668_j;

    protected LegacyMovingSound(ResourceLocation soundLocation) {
        this.field_147664_a = soundLocation;
    }

    public abstract void func_73660_a();

    @Override
    public ResourceLocation func_147650_b() {
        return field_147664_a != null ? field_147664_a : LegacyISound.super.func_147650_b();
    }

    @Override
    public boolean func_147657_c() {
        return field_147659_g;
    }

    @Override
    public int func_147652_d() {
        return field_147665_h;
    }

    @Override
    public float func_147653_e() {
        return field_147662_b;
    }

    @Override
    public float func_147655_f() {
        return field_147663_c;
    }

    @Override
    public float func_147649_g() {
        return field_147660_d;
    }

    @Override
    public float func_147654_h() {
        return field_147661_e;
    }

    @Override
    public float func_147651_i() {
        return field_147658_f;
    }

    @Override
    public AttenuationType func_147656_j() {
        return field_147666_i;
    }

    @Override
    public boolean isStopped() {
        return field_147668_j;
    }

    @Override
    public void tick() {
        func_73660_a();
    }
}
