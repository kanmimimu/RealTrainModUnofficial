package com.myname.legacyloader.bridge.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 1.7.10縺ｮ ICommandSender 莠呈鋤繧ｯ繝ｩ繧ｹ
 */
public class LegacyCommandSender {

    private final CommandSourceStack source;

    public LegacyCommandSender(CommandSourceStack source) {
        this.source = source;
    }

    public LegacyCommandSender() {
        this.source = null;
    }

    /**
     * 繧ｳ繝槭Φ繝蛾∽ｿ｡閠・・蜷榊燕繧貞叙蠕・
     */
    public String getCommandSenderName() {
        if (source != null) {
            return source.getTextName();
        }
        return "Server";
    }

    /**
     * 1.7.10 SRG: func_70005_c_
     */
    public String func_70005_c_() {
        return getCommandSenderName();
    }

    /**
     * 繝√Ε繝・ヨ繝｡繝・そ繝ｼ繧ｸ繧定ｿｽ蜉
     */
    public void addChatMessage(String message) {
        if (source != null) {
            source.sendSuccess(() -> Component.literal(message), false);
        }
    }

    public void addChatMessage(Component message) {
        if (source != null) {
            source.sendSuccess(() -> message, false);
        }
    }

    /**
     * 1.7.10 SRG: func_145747_a
     */
    public void func_145747_a(Object message) {
        if (message instanceof Component) {
            addChatMessage((Component) message);
        } else if (message instanceof String) {
            addChatMessage((String) message);
        } else if (message != null) {
            addChatMessage(message.toString());
        }
    }

    /**
     * 繧ｳ繝槭Φ繝峨ｒ菴ｿ逕ｨ縺ｧ縺阪ｋ縺・
     */
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        if (source != null) {
            return source.hasPermission(permLevel);
        }
        return true;
    }

    /**
     * 1.7.10 SRG: func_70003_b
     */
    public boolean func_70003_b(int permLevel, String commandName) {
        return canCommandSenderUseCommand(permLevel, commandName);
    }

    /**
     * 菴咲ｽｮ繧貞叙蠕・
     */
    public Vec3 getPositionVector() {
        if (source != null) {
            return source.getPosition();
        }
        return Vec3.ZERO;
    }

    /**
     * 1.7.10 SRG: func_70014_b (ChunkCoordinates)
     */
    public Object func_70014_b() {
        return getPositionVector();
    }

    /**
     * 繝ｯ繝ｼ繝ｫ繝峨ｒ蜿門ｾ・
     */
    public Object getEntityWorld() {
        if (source != null) {
            return source.getLevel();
        }
        return null;
    }

    /**
     * 1.7.10 SRG: func_130014_f_
     */
    public Object func_130014_f_() {
        return getEntityWorld();
    }

    /**
     * 繧ｨ繝ｳ繝・ぅ繝・ぅ繧貞叙蠕・
     */
    public Entity getEntity() {
        if (source != null) {
            return source.getEntity();
        }
        return null;
    }

    /**
     * 繝励Ξ繧､繝､繝ｼ縺ｨ縺励※蜿門ｾ・
     */
    public ServerPlayer getPlayer() {
        Entity entity = getEntity();
        if (entity instanceof ServerPlayer) {
            return (ServerPlayer) entity;
        }
        return null;
    }

    /**
     * 蜀・Κ縺ｮCommandSourceStack繧貞叙蠕・
     */
    public CommandSourceStack getSource() {
        return source;
    }

    /**
     * 繧ｵ繝ｼ繝舌・繧ｳ繝ｳ繧ｽ繝ｼ繝ｫ縺九←縺・°
     */
    public boolean isServer() {
        return source != null && source.getEntity() == null;
    }

    /**
     * 繝励Ξ繧､繝､繝ｼ縺九←縺・°
     */
    public boolean isPlayer() {
        return getPlayer() != null;
    }
}