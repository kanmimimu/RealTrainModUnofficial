package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class LegacyItemAxe extends AxeItem {
    public String legacyTextureName;

    public LegacyItemAxe(Tier tier) {
        super(tier, new Item.Properties().attributes(AxeItem.createAttributes(tier, 6.0F, -3.1F)));
        LegacyGameRegistry.trackItem(this);
    }

    public LegacyItemAxe(LegacyTier tier) { this((Tier) tier); }
    public LegacyItemAxe(Object tier) { this((Tier) tier); }

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
