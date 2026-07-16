package com.myname.legacyloader.bridge.server;

import com.myname.legacyloader.bridge.command.LegacyCommandSender;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Bridge for 1.7.10 MinecraftServer access.
 */
public class LegacyMinecraftServer extends LegacyCommandSender {

    private static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static MinecraftServer func_71276_C() {
        return getServer();
    }

    public static String func_71265_f() {
        MinecraftServer server = getServer();
        return server != null ? server.getServerVersion() : "1.21.1";
    }

    public static int func_71239_e() {
        MinecraftServer server = getServer();
        return server != null ? server.getPlayerCount() : 0;
    }
}
