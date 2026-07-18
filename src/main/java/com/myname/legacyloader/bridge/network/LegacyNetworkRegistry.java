package com.myname.legacyloader.bridge.network;

import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.7.10縺ｮ cpw.mods.fml.common.network.NetworkRegistry 莠呈鋤繧ｯ繝ｩ繧ｹ
 */
public class LegacyNetworkRegistry {

    // 笘・㍾隕・ 髱咏噪繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ
    public static final LegacyNetworkRegistry INSTANCE = new LegacyNetworkRegistry();
    public static final AttributeKey<String> FML_CHANNEL = AttributeKey.valueOf("fml:channel");

    private final Map<Object, IGuiHandler> guiHandlers = new HashMap<>();
    private final Map<String, LegacySimpleNetworkWrapper> channels = new HashMap<>();

    // 繝励Λ繧､繝吶・繝医さ繝ｳ繧ｹ繝医Λ繧ｯ繧ｿ・医す繝ｳ繧ｰ繝ｫ繝医Φ・・
    private LegacyNetworkRegistry() {
    }

    /**
     * GUI繝上Φ繝峨Λ繧堤匳骭ｲ
     */
    public void registerGuiHandler(Object mod, IGuiHandler handler) {
        guiHandlers.put(mod, handler);
        System.out.println("LegacyLoader: Registered GUI handler for " + mod.getClass().getSimpleName());
    }

    /**
     * 1.7.10 SRG
     */
    public void func_145938_a(Object mod, IGuiHandler handler) {
        registerGuiHandler(mod, handler);
    }

    /**
     * 笘・ｿｮ豁｣: 繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ繝｡繧ｽ繝・ラ縺ｨ縺励※縺ｮnewSimpleChannel
     * 菴ｿ逕ｨ萓・ NetworkRegistry.INSTANCE.newSimpleChannel("channelName")
     */
    public LegacySimpleNetworkWrapper newSimpleChannel(String channelName) {
        LegacySimpleNetworkWrapper wrapper = new LegacySimpleNetworkWrapper(channelName);
        channels.put(channelName, wrapper);
        System.out.println("LegacyLoader: Created network channel: " + channelName);
        return wrapper;
    }

    public EnumMap<LegacySide, LegacyFMLEmbeddedChannel> newChannel(String channelName, ChannelHandler... handlers) {
        EnumMap<LegacySide, LegacyFMLEmbeddedChannel> map = new EnumMap<>(LegacySide.class);
        LegacyFMLEmbeddedChannel client = new LegacyFMLEmbeddedChannel(handlers);
        LegacyFMLEmbeddedChannel server = new LegacyFMLEmbeddedChannel();
        client.attr(FML_CHANNEL).set(channelName);
        server.attr(FML_CHANNEL).set(channelName);
        map.put(LegacySide.CLIENT, client);
        map.put(LegacySide.SERVER, server);
        System.out.println("LegacyLoader: Created embedded network channel: " + channelName);
        return map;
    }

    /**
     * MOD繧ｪ繝悶ず繧ｧ繧ｯ繝井ｻ倥″繝舌・繧ｸ繝ｧ繝ｳ
     */
    public LegacySimpleNetworkWrapper newSimpleChannel(Object mod, String channelName) {
        return newSimpleChannel(channelName);
    }

    /**
     * 笘・ｿｽ蜉: 髱咏噪繝｡繧ｽ繝・ラ縺ｨ縺励※縺ｮnewSimpleChannel・井ｸ｡譁ｹ縺ｫ蟇ｾ蠢懶ｼ・
     */
    public static LegacySimpleNetworkWrapper newSimpleChannelStatic(String channelName) {
        return INSTANCE.newSimpleChannel(channelName);
    }

    /**
     * GUI繧帝幕縺・
     */
    public void openGui(Object mod, int guiId, Player player, int x, int y, int z) {
        IGuiHandler handler = guiHandlers.get(mod);
        if (handler != null && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            Object container = handler.getServerGuiElement(guiId, player, player.level(), x, y, z);
            if (container instanceof AbstractContainerMenu) {
                System.out.println("LegacyLoader: Opening GUI " + guiId + " for " + player.getName().getString());
            }
        }
    }

    /**
     * 髱咏噪繝｡繧ｽ繝・ラ縺ｨ縺励※縺ｮopenGui・・MLCommonHandler遲峨°繧牙他縺ｰ繧後ｋ蝣ｴ蜷茨ｼ・
     */
    public static void openGuiStatic(Object mod, int guiId, Player player, int x, int y, int z) {
        INSTANCE.openGui(mod, guiId, player, x, y, z);
    }

    /**
     * 繝√Ε繝ｳ繝阪Ν繧貞叙蠕・
     */
    public LegacySimpleNetworkWrapper getChannel(String name) {
        return channels.get(name);
    }

    /**
     * GUI繝上Φ繝峨Λ繧貞叙蠕・
     */
    public IGuiHandler getGuiHandler(Object mod) {
        return guiHandlers.get(mod);
    }

    /**
     * 笘・ｿｽ蜉: instance() 繝｡繧ｽ繝・ラ・井ｸ驛ｨ縺ｮMOD縺後％繧後ｒ菴ｿ縺・ｼ・
     */
    public static LegacyNetworkRegistry instance() {
        return INSTANCE;
    }
}
