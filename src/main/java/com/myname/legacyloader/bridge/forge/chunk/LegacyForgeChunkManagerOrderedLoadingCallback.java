package com.myname.legacyloader.bridge.forge.chunk;

import net.minecraft.world.level.Level;

import java.util.List;

public interface LegacyForgeChunkManagerOrderedLoadingCallback extends LegacyForgeChunkManagerLoadingCallback {
    List<LegacyTicket> ticketsLoaded(List<LegacyTicket> tickets, Level world, int maxTicketCount);
}
