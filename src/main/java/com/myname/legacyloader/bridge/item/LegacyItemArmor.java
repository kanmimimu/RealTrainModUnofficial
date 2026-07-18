package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public class LegacyItemArmor extends ArmorItem {
    public String legacyTextureName;

    public LegacyItemArmor(LegacyArmorMaterial material, int renderIndex, int armorType) {
        super(material.asHolder(), getTypeFromInt(armorType), new Item.Properties());
        LegacyGameRegistry.trackItem(this);
    }

    public LegacyItemArmor(Object material, int renderIndex, int armorType) {
        this((LegacyArmorMaterial) material, renderIndex, armorType);
    }

    private static Type getTypeFromInt(int type) {
        return switch (type) {
            case 0 -> Type.HELMET;
            case 1 -> Type.CHESTPLATE;
            case 2 -> Type.LEGGINGS;
            case 3 -> Type.BOOTS;
            default -> Type.HELMET;
        };
    }

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
