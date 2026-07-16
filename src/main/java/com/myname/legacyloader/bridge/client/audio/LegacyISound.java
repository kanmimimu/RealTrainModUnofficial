package com.myname.legacyloader.bridge.client.audio;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public interface LegacyISound extends SoundInstance {
    enum AttenuationType {
        NONE(0),
        LINEAR(2);

        private final int type;

        AttenuationType(int type) {
            this.type = type;
        }

        public int getTypeInt() {
            return type;
        }

        public int func_148586_a() {
            return type;
        }

        public SoundInstance.Attenuation toModern() {
            return this == NONE ? SoundInstance.Attenuation.NONE : SoundInstance.Attenuation.LINEAR;
        }
    }

    default ResourceLocation func_147650_b() {
        return ResourceLocation.withDefaultNamespace("missing_sound");
    }

    default boolean func_147657_c() {
        return false;
    }

    default int func_147652_d() {
        return 0;
    }

    default float func_147653_e() {
        return 1.0F;
    }

    default float func_147655_f() {
        return 1.0F;
    }

    default float func_147649_g() {
        return 0.0F;
    }

    default float func_147654_h() {
        return 0.0F;
    }

    default float func_147651_i() {
        return 0.0F;
    }

    default AttenuationType func_147656_j() {
        return AttenuationType.LINEAR;
    }

    @Override
    default ResourceLocation getLocation() {
        return func_147650_b();
    }

    @Override
    default WeighedSoundEvents resolve(SoundManager soundManager) {
        return soundManager != null ? soundManager.getSoundEvent(getLocation()) : null;
    }

    @Override
    default Sound getSound() {
        return SoundManager.EMPTY_SOUND;
    }

    @Override
    default SoundSource getSource() {
        return SoundSource.MASTER;
    }

    @Override
    default boolean isLooping() {
        return func_147657_c();
    }

    @Override
    default boolean isRelative() {
        return false;
    }

    @Override
    default int getDelay() {
        return func_147652_d();
    }

    @Override
    default float getVolume() {
        return func_147653_e();
    }

    @Override
    default float getPitch() {
        return func_147655_f();
    }

    @Override
    default double getX() {
        return func_147649_g();
    }

    @Override
    default double getY() {
        return func_147654_h();
    }

    @Override
    default double getZ() {
        return func_147651_i();
    }

    @Override
    default SoundInstance.Attenuation getAttenuation() {
        AttenuationType attenuation = func_147656_j();
        return attenuation != null ? attenuation.toModern() : SoundInstance.Attenuation.LINEAR;
    }
}
