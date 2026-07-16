package com.myname.legacyloader.bridge.util;

import com.myname.legacyloader.bridge.item.LegacyArmorMaterial;
import com.myname.legacyloader.bridge.item.LegacyTier;

public class LegacyEnumHelper {

    public static <T extends Enum<T>> T addEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object[] paramValues) {
        T[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return null;
        }
        for (T constant : constants) {
            if (constant.name().equals(enumName) || constant.name().equals("CUSTOM")) {
                return constant;
            }
        }
        return constants[0];
    }

    // 繝・・繝ｫ邏譚・
    public static LegacyTier addToolMaterial(String name, int harvestLevel, int maxUses, float efficiency, float damage, int enchantability) {
        return new LegacyTier(harvestLevel, maxUses, efficiency, damage, enchantability);
    }

    // 笘・ｿｽ蜉: 髦ｲ蜈ｷ邏譚・
    public static LegacyArmorMaterial addArmorMaterial(String name, int durability, int[] reductionAmounts, int enchantability) {
        return new LegacyArmorMaterial(name, durability, reductionAmounts, enchantability);
    }
}
