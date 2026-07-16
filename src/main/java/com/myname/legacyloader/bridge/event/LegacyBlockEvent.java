package com.myname.legacyloader.bridge.event;

import com.myname.legacyloader.bridge.fml.LegacyEvent;
import com.myname.legacyloader.bridge.forge.LegacyBlockSnapshot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class LegacyBlockEvent extends LegacyEvent {
    public final int x;
    public final int y;
    public final int z;
    public final Object world;
    public final Block block;
    public final int blockMetadata;

    public LegacyBlockEvent(int x, int y, int z, Object world, Block block, int blockMetadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.block = block;
        this.blockMetadata = blockMetadata;
    }

    public static class BreakEvent extends LegacyBlockEvent {
        public final Player player;

        public BreakEvent(int x, int y, int z, Level world, Block block, int blockMetadata, Player player) {
            super(x, y, z, world, block, blockMetadata);
            this.player = player;
        }
    }

    public static class PlaceEvent extends LegacyBlockEvent {
        public final Object blockSnapshot;
        public final Block placedAgainst;
        public final Player player;

        public PlaceEvent(Object blockSnapshot, Block placedAgainst, Player player) {
            super(0, 0, 0, null, null, 0);
            this.blockSnapshot = blockSnapshot;
            this.placedAgainst = placedAgainst;
            this.player = player;
        }

        public PlaceEvent(LegacyBlockSnapshot blockSnapshot, Block placedAgainst, Player player) {
            this((Object) blockSnapshot, placedAgainst, player);
        }
    }
}
