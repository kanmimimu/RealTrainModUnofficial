package com.myname.legacyloader.bridge.command;

import java.util.Collections;
import java.util.List;

public interface LegacyICommand extends Comparable<Object> {
    default String getCommandName() {
        return "";
    }

    default String func_71517_b() {
        return getCommandName();
    }

    default String getCommandUsage(LegacyCommandSender sender) {
        return "";
    }

    default String func_71518_a(LegacyCommandSender sender) {
        return getCommandUsage(sender);
    }

    default void processCommand(LegacyCommandSender sender, String[] args) {}

    default void func_71515_b(LegacyCommandSender sender, String[] args) {
        processCommand(sender, args);
    }

    default boolean canCommandSenderUseCommand(LegacyCommandSender sender) {
        return true;
    }

    default boolean func_71519_b(LegacyCommandSender sender) {
        return canCommandSenderUseCommand(sender);
    }

    default List<String> getCommandAliases() {
        return Collections.emptyList();
    }

    default List<String> func_71514_a() {
        return getCommandAliases();
    }

    default List<String> addTabCompletionOptions(LegacyCommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    default List<String> func_71516_a(LegacyCommandSender sender, String[] args) {
        return addTabCompletionOptions(sender, args);
    }

    @Override
    default int compareTo(Object other) {
        if (other instanceof LegacyICommand) {
            return getCommandName().compareTo(((LegacyICommand) other).getCommandName());
        }
        return 0;
    }
}
