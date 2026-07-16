package com.myname.legacyloader.bridge.command;

public class LegacyServerCommand {
    public final String command;
    public final Object sender;

    public LegacyServerCommand(String command, Object sender) {
        this.command = command;
        this.sender = sender;
    }
}
