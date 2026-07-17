package jp.ngt.rtm.item;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 本家 jp.ngt.rtm.RTMItem のアイテム登録 (段階的移植、1.21 DeferredRegister 版)。
 */
public final class RTMItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(Registries.ITEM, RealTrainModUnofficial.MODID);

    //本家: RTMItem.itemLargeRail
    public static final RegistryObject<ItemRail> ITEM_LARGE_RAIL =
            REGISTER.register("item_large_rail", () -> new ItemRail(new Item.Properties()));

    private RTMItems() {
    }
}
