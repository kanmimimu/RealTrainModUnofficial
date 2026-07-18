package com.myname.legacyloader.bridge.event;

import com.mojang.brigadier.CommandDispatcher;
import com.myname.legacyloader.bridge.command.LegacyCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * 1.7.10縺ｮ FMLServerStartingEvent 莠呈鋤
 */
public class LegacyServerStartingEvent {

    private final ServerStartingEvent event;

    public LegacyServerStartingEvent(ServerStartingEvent event) {
        this.event = event;
    }

    /**
     * 繧ｳ繝槭Φ繝峨ｒ逋ｻ骭ｲ
     */
    public void registerServerCommand(LegacyCommand command) {
        if (event != null && event.getServer() != null) {
            CommandDispatcher<CommandSourceStack> dispatcher =
                    event.getServer().getCommands().getDispatcher();
            command.registerToBrigadier(dispatcher);
            System.out.println("LegacyLoader: Registered command: " + command.getCommandName());
        }
    }

    /**
     * 1.7.10 SRG: func_152373_a
     */
    public void func_152373_a(LegacyCommand command) {
        registerServerCommand(command);
    }

    public void func_152373_a(Object command) {
        if (command instanceof LegacyCommand) {
            registerServerCommand((LegacyCommand) command);
        }
    }

    /**
     * 繧ｵ繝ｼ繝舌・繧貞叙蠕・
     */
    public Object getServer() {
        return event != null ? event.getServer() : null;
    }

    /**
     * 1.7.10 SRG: func_71encoding_e
     */
    public Object func_71encoding_e() {
        return getServer();
    }
}