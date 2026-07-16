package com.myname.legacyloader.bridge.fml;

import net.minecraft.world.entity.player.Player;

public class LegacyPlayerEvent extends LegacyEvent {
    public final Player entityPlayer;

    public LegacyPlayerEvent() {
        this(null);
    }

    public LegacyPlayerEvent(Player player) {
        this.entityPlayer = player;
    }
}
