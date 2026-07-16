package com.myname.legacyloader.bridge.enchantment;

public class LegacyEnchantmentData {
    public final LegacyEnchantment enchantmentobj;
    public final int enchantmentLevel;
    public final int itemWeight;

    public LegacyEnchantmentData(LegacyEnchantment enchantment, int level) {
        this.enchantmentobj = enchantment;
        this.enchantmentLevel = level;
        this.itemWeight = enchantment == null ? 0 : enchantment.getWeight();
    }

    public LegacyEnchantmentData(int enchantmentId, int level) {
        this(enchantmentId >= 0 && enchantmentId < LegacyEnchantment.enchantmentsList.length
                ? LegacyEnchantment.enchantmentsList[enchantmentId]
                : null, level);
    }
}
