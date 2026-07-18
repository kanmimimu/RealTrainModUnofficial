package com.myname.legacyloader.bridge.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10縺ｮ ICommand / CommandBase 莠呈鋤繧ｯ繝ｩ繧ｹ
 */
public abstract class LegacyCommand implements LegacyICommand {

    /**
     * 繧ｳ繝槭Φ繝牙錐繧貞叙蠕・
     */
    public abstract String getCommandName();

    /**
     * 1.7.10 SRG: func_71517_b
     */
    public String func_71517_b() {
        return getCommandName();
    }

    /**
     * 繧ｳ繝槭Φ繝峨・菴ｿ逕ｨ譁ｹ豕輔ｒ蜿門ｾ・
     */
    public abstract String getCommandUsage(LegacyCommandSender sender);

    /**
     * 1.7.10 SRG: func_71518_a
     */
    public String func_71518_a(LegacyCommandSender sender) {
        return getCommandUsage(sender);
    }

    public String func_71518_a(Object sender) {
        if (sender instanceof LegacyCommandSender) {
            return getCommandUsage((LegacyCommandSender) sender);
        }
        return getCommandUsage(null);
    }

    /**
     * 繧ｳ繝槭Φ繝峨ｒ螳溯｡・
     */
    public abstract void processCommand(LegacyCommandSender sender, String[] args);

    /**
     * 1.7.10 SRG: func_71515_b
     */
    public void func_71515_b(LegacyCommandSender sender, String[] args) {
        processCommand(sender, args);
    }

    public void func_71515_b(Object sender, String[] args) {
        if (sender instanceof LegacyCommandSender) {
            processCommand((LegacyCommandSender) sender, args);
        }
    }

    /**
     * 繧ｳ繝槭Φ繝峨ｒ菴ｿ逕ｨ縺ｧ縺阪ｋ縺・
     */
    public boolean canCommandSenderUseCommand(LegacyCommandSender sender) {
        return true;
    }

    /**
     * 1.7.10 SRG: func_71519_b
     */
    public boolean func_71519_b(LegacyCommandSender sender) {
        return canCommandSenderUseCommand(sender);
    }

    public boolean func_71519_b(Object sender) {
        if (sender instanceof LegacyCommandSender) {
            return canCommandSenderUseCommand((LegacyCommandSender) sender);
        }
        return true;
    }

    /**
     * 繧ｨ繧､繝ｪ繧｢繧ｹ繧貞叙蠕・
     */
    public List<String> getCommandAliases() {
        return new ArrayList<>();
    }

    /**
     * 1.7.10 SRG: func_71514_a
     */
    public List<String> func_71514_a() {
        return getCommandAliases();
    }

    /**
     * 繧ｿ繝冶｣懷ｮ・
     */
    public List<String> addTabCompletionOptions(LegacyCommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    /**
     * 1.7.10 SRG: func_71516_a
     */
    public List<String> func_71516_a(LegacyCommandSender sender, String[] args) {
        return addTabCompletionOptions(sender, args);
    }

    /**
     * 蠢・ｦ√↑讓ｩ髯舌Ξ繝吶Ν
     */
    public int getRequiredPermissionLevel() {
        return 4;
    }

    /**
     * 豈碑ｼ・畑
     */
    public int compareTo(LegacyCommand other) {
        return this.getCommandName().compareTo(other.getCommandName());
    }

    public int compareTo(Object other) {
        if (other instanceof LegacyCommand) {
            return compareTo((LegacyCommand) other);
        }
        return 0;
    }

    // === 繝倥Ν繝代・繝｡繧ｽ繝・ラ・・ommandBase縺九ｉ・・===

    /**
     * 繝励Ξ繧､繝､繝ｼ繧貞叙蠕・
     */
    public static ServerPlayer getPlayer(LegacyCommandSender sender, String name) {
        // 螳溯｣・・蠢・ｦ√↓蠢懊§縺ｦ
        return null;
    }

    /**
     * 繝｡繝・そ繝ｼ繧ｸ繧帝∽ｿ｡
     */
    public static void sendMessage(LegacyCommandSender sender, String message) {
        if (sender != null) {
            sender.addChatMessage(message);
        }
    }

    /**
     * 繧ｨ繝ｩ繝ｼ繝｡繝・そ繝ｼ繧ｸ繧帝∽ｿ｡
     */
    public static void sendError(LegacyCommandSender sender, String message) {
        if (sender != null) {
            sender.addChatMessage("ﾂｧc" + message);
        }
    }

    /**
     * 謨ｰ蛟､繧偵ヱ繝ｼ繧ｹ
     */
    public static int parseInt(String str) throws LegacyCommandException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new LegacyCommandException("commands.generic.num.invalid", str);
        }
    }

    public static int parseIntWithMin(String str, int min) throws LegacyCommandException {
        int value = parseInt(str);
        if (value < min) {
            throw new LegacyCommandException("commands.generic.num.tooSmall", value, min);
        }
        return value;
    }

    public static int parseIntBounded(String str, int min, int max) throws LegacyCommandException {
        int value = parseInt(str);
        if (value < min || value > max) {
            throw new LegacyCommandException("commands.generic.num.invalid", str);
        }
        return value;
    }

    public static double parseDouble(String str) throws LegacyCommandException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new LegacyCommandException("commands.generic.num.invalid", str);
        }
    }

    public static boolean parseBoolean(String str) throws LegacyCommandException {
        if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
            return true;
        } else if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
            return false;
        }
        throw new LegacyCommandException("commands.generic.boolean.invalid", str);
    }

    // === 1.20.1縺ｸ縺ｮ逋ｻ骭ｲ繝倥Ν繝代・ ===

    /**
     * 縺薙・繧ｳ繝槭Φ繝峨ｒBrigadier縺ｫ逋ｻ骭ｲ
     */
    public void registerToBrigadier(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(getCommandName())
                        .requires(source -> source.hasPermission(getRequiredPermissionLevel()))
                        .executes(context -> {
                            LegacyCommandSender sender = new LegacyCommandSender(context.getSource());
                            processCommand(sender, new String[0]);
                            return 1;
                        })
                // 蠑墓焚莉倥″繝舌・繧ｸ繝ｧ繝ｳ繧りｿｽ蜉蜿ｯ閭ｽ
        );
    }
}
