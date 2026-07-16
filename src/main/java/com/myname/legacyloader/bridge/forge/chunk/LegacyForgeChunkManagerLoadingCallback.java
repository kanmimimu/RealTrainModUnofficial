package com.myname.legacyloader.bridge.forge.chunk;

import net.minecraft.world.level.Level;
import java.util.List;

public interface LegacyForgeChunkManagerLoadingCallback {
    // 1.7.10: ticketsLoaded(List<Ticket> tickets, World world)
    void ticketsLoaded(List<LegacyTicket> tickets, Level world);
}