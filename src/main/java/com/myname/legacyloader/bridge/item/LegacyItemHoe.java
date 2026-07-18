package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class LegacyItemHoe extends HoeItem {
    public String legacyTextureName;

    public LegacyItemHoe(Tier tier) {
        super(tier, new Item.Properties().attributes(HoeItem.createAttributes(tier, -2.0F, -1.0F)));
        LegacyGameRegistry.trackItem(this);
    }

    public LegacyItemHoe(LegacyTier tier) { this((Tier) tier); }
    public LegacyItemHoe(Object tier) { this((Tier) tier); }

    public Item setTextureName(String name) {
        this.legacyTextureName = name;
        return this;
    }
    public Item setUnlocalizedName(String name) { return this; }
    public Item setCreativeTab(LegacyCreativeTab tab) {
        if (tab != null) tab.addItem(this);
        return this;
    }
    public Item setMaxStackSize(int size) { return this; }
    public Item setMaxDamage(int damage) { return this; }
    public Item setFull3D() { return this; }

    public Item func_111206_d(String name) { return setTextureName(name); }
    public Item func_77655_b(String name) { return setUnlocalizedName(name); }
    public Item func_77637_a(LegacyCreativeTab tab) { return setCreativeTab(tab); }
    public Item func_77625_d(int size) { return setMaxStackSize(size); }
    public Item func_77656_e(int damage) { return setMaxDamage(damage); }
    public Item func_77668_e() { return setFull3D(); }
}
