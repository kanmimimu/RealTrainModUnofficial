package com.myname.legacyloader.bridge.registry;

import net.minecraft.resources.ResourceLocation;
import java.util.Random;

public class LegacyVillagerRegistry {

    public static LegacyVillagerRegistry instance() {
        return new LegacyVillagerRegistry();
    }

    public void registerVillageTradeHandler(int villagerId, IVillageTradeHandler handler) {
    }

    public void registerVillagerId(int id) {
    }

    // 笘・ｿｽ蜉: 繧ｹ繧ｭ繝ｳ逋ｻ骭ｲ (StationsMod縺ｧ菴ｿ逕ｨ)
    public void registerVillagerSkin(int villagerId, ResourceLocation skin) {
        // System.out.println("LegacyLoader: Registered villager skin for ID " + villagerId + ": " + skin);
    }

    public interface IVillageTradeHandler {
        void manipulateTradesForVillager(net.minecraft.world.entity.npc.Villager villager, net.minecraft.world.item.trading.MerchantOffers recipeList, Random random);
    }
}