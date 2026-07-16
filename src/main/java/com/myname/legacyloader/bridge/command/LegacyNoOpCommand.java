package com.myname.legacyloader.bridge.command;

import java.util.Collections;
import java.util.List;

public class LegacyNoOpCommand extends LegacyCommand {
    private final String name;

    public LegacyNoOpCommand() {
        this("legacy");
    }

    public LegacyNoOpCommand(String name) {
        this.name = name == null ? "legacy" : name;
    }

    @Override
    public String getCommandName() {
        return this.name;
    }

    @Override
    public String getCommandUsage(LegacyCommandSender sender) {
        return "/" + this.name;
    }

    @Override
    public void processCommand(LegacyCommandSender sender, String[] args) {
    }

    @Override
    public List<String> addTabCompletionOptions(LegacyCommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
