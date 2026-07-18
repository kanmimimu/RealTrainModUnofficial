package com.myname.legacyloader.bridge.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 1.7.10縺ｮ IGuiHandler 莠呈鋤繧､繝ｳ繧ｿ繝ｼ繝輔ぉ繝ｼ繧ｹ
 */
public interface IGuiHandler {

    /**
     * 繧ｵ繝ｼ繝舌・蛛ｴ縺ｮGUI隕∫ｴ繧貞叙蠕・
     */
    Object getServerGuiElement(int id, Player player, Level world, int x, int y, int z);

    /**
     * 繧ｯ繝ｩ繧､繧｢繝ｳ繝亥・縺ｮGUI隕∫ｴ繧貞叙蠕・
     */
    Object getClientGuiElement(int id, Player player, Level world, int x, int y, int z);

    // === 1.7.10 SRG蜷阪お繧､繝ｪ繧｢繧ｹ ===

    default Object func_147129_a(int id, Object player, Object world, int x, int y, int z) {
        if (player instanceof Player && world instanceof Level) {
            return getServerGuiElement(id, (Player) player, (Level) world, x, y, z);
        }
        return null;
    }

    default Object func_147128_b(int id, Object player, Object world, int x, int y, int z) {
        if (player instanceof Player && world instanceof Level) {
            return getClientGuiElement(id, (Player) player, (Level) world, x, y, z);
        }
        return null;
    }
}