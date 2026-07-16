package com.myname.legacyloader.bridge.command;

public interface LegacyIAdminCommand {
    default void notifyAdmins(Object sender, LegacyICommand command, int flags, String message, Object... args) {
    }
}
