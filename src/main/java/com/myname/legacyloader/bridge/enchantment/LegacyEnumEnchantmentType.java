package com.myname.legacyloader.bridge.enchantment;

public enum LegacyEnumEnchantmentType {
    all,
    armor,
    armor_feet,
    armor_legs,
    armor_torso,
    armor_head,
    weapon,
    digger,
    fishing_rod,
    breakable,
    bow;

    public boolean canEnchantItem(Object item) {
        if (this == all) {
            return true;
        }
        if (this == breakable) {
            try {
                Object value = item.getClass().getMethod("isDamageable").invoke(item);
                return Boolean.TRUE.equals(value);
            } catch (Throwable ignored) {
                return true;
            }
        }
        return item != null;
    }
}
