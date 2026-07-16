package com.myname.legacyloader.bridge.client.audio;

import net.minecraft.client.Minecraft;

public class LegacySoundHandler {
    private final Minecraft minecraft;

    public LegacySoundHandler(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void func_147682_a(LegacyISound sound) {
        playSound(sound);
    }

    public void playSound(LegacyISound sound) {
        if (sound != null && minecraft != null) {
            minecraft.getSoundManager().play(sound);
        }
    }
}
