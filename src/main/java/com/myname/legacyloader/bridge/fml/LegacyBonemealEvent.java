package com.myname.legacyloader.bridge.fml;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class LegacyBonemealEvent extends LegacyPlayerEvent {
    public final Level world;
    public final Block block;
    public final int x;
    public final int y;
    public final int z;

    public LegacyBonemealEvent() {
        this(null, null, null, 0, 0, 0);
    }

    public LegacyBonemealEvent(Player player, Level world, Block block, int x, int y, int z) {
        super(player);
        this.world = world;
        this.block = block;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
