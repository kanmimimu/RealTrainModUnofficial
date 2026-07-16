package com.myname.legacyloader.bridge.forge;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class LegacyFakePlayerFactory {
    private LegacyFakePlayerFactory() {
    }

    public static Player get(ServerLevel level, GameProfile profile) {
        if (level == null || level.getServer() == null || profile == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(profile.getId());
    }
}
