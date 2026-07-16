package com.myname.legacyloader.bridge.command;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LegacyCommandHandler implements LegacyCommandManager {
    protected final Map<String, LegacyICommand> commandMap = new LinkedHashMap<>();
    protected final java.util.Set<LegacyICommand> commandSet = new java.util.LinkedHashSet<>();

    public LegacyICommand registerCommand(LegacyICommand command) {
        if (command == null) {
            return null;
        }
        this.commandMap.put(command.getCommandName(), command);
        for (String alias : command.getCommandAliases()) {
            this.commandMap.put(alias, command);
        }
        this.commandSet.add(command);
        return command;
    }

    public LegacyICommand func_71560_a(LegacyICommand command) {
        return registerCommand(command);
    }

    @Override
    public int executeCommand(Object sender, String commandLine) {
        if (commandLine == null || commandLine.isEmpty()) {
            return 0;
        }
        String raw = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
        String[] parts = raw.split(" ");
        LegacyICommand command = this.commandMap.get(parts[0]);
        if (command == null) {
            return 0;
        }
        String[] args = new String[Math.max(0, parts.length - 1)];
        if (args.length > 0) {
            System.arraycopy(parts, 1, args, 0, args.length);
        }
        command.func_71515_b(sender instanceof LegacyCommandSender ? (LegacyCommandSender) sender : null, args);
        return 1;
    }

    public Map<String, LegacyICommand> getCommands() {
        return Collections.unmodifiableMap(this.commandMap);
    }

    public Map<String, LegacyICommand> func_71555_a() {
        return getCommands();
    }

    public Collection<LegacyICommand> getPossibleCommands(Object sender) {
        return Collections.unmodifiableSet(this.commandSet);
    }

    public Collection<LegacyICommand> func_71557_a(Object sender) {
        return getPossibleCommands(sender);
    }
}
