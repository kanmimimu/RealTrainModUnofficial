package com.myname.legacyloader.bridge.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import java.lang.reflect.Field;

public class LegacyItemHelper {

    private static LegacyItem cast(Item item) {
        return (item instanceof LegacyItem) ? (LegacyItem) item : null;
    }

    // === 蝓ｺ譛ｬ繝｡繧ｽ繝・ラ ===

    public static Item setUnlocalizedName(Item item, String name) {
        if (item == null) return null;
        LegacyItem li = cast(item);
        if (li != null) {
            li.setUnlocalizedName(name);
        }
        return item;
    }

    public static Item setTextureName(Item item, String name) {
        if (item == null) return null;
        LegacyItem li = cast(item);
        if (li != null) {
            li.setTextureName(name);
        }
        return item;
    }

    public static Item setCreativeTab(Item item, LegacyCreativeTab tab) {
        if (item == null) return null;
        LegacyItem li = cast(item);
        if (li != null) {
            li.setCreativeTab(tab);
        } else if (tab != null) {
            // LegacyItem莉･螟悶〒繧ゅち繝悶↓霑ｽ蜉繧定ｩｦ縺ｿ繧・
            tab.addItem(item);
        }
        return item;
    }

    public static Item setMaxStackSize(Item item, int size) {
        if (item == null) return null;
        try {
            Field f = findField(Item.class, "maxStackSize", "f_41370_");
            if (f != null) {
                f.setAccessible(true);
                f.setInt(item, size);
            }
        } catch (Exception e) {
            // 辟｡隕・
        }
        return item;
    }

    public static Item setMaxDamage(Item item, int damage) {
        if (item == null) return null;
        try {
            Field f = findField(Item.class, "maxDamage", "f_41371_");
            if (f != null) {
                f.setAccessible(true);
                f.setInt(item, damage);
            }
        } catch (Exception e) {
            // 辟｡隕・
        }
        return item;
    }

    public static Item setFull3D(Item item) {
        // 1.20.1縺ｧ縺ｯ縺薙・讎ょｿｵ縺後↑縺・・縺ｧ繝繝溘・
        return item;
    }

    public static Item setNoRepair(Item item) {
        // 繝繝溘・
        return item;
    }

    public static Item setContainerItem(Item item, Item container) {
        // 繝繝溘・
        return item;
    }

    public static void setHarvestLevel(Item item, String toolClass, int level) {
        // 1.7.10 Item#setHarvestLevel is metadata used by old tool checks.
        // Modern items no longer expose this directly, so keep it as a no-op.
    }

    // === SRG蜷阪お繧､繝ｪ繧｢繧ｹ ===

    public static Item func_77655_b(Item item, String name) { return setUnlocalizedName(item, name); }
    public static Item func_111206_d(Item item, String name) { return setTextureName(item, name); }
    public static Item func_77637_a(Item item, LegacyCreativeTab tab) { return setCreativeTab(item, tab); }
    public static Item func_77625_d(Item item, int size) { return setMaxStackSize(item, size); }
    public static Item func_77656_e(Item item, int damage) { return setMaxDamage(item, damage); }
    public static Item func_77668_e(Item item) { return setFull3D(item); }
    public static Item func_77664_n(Item item) { return setNoRepair(item); }
    public static int func_77612_l(Item item) {
        if (item == null) return 64;
        try { return new net.minecraft.world.item.ItemStack(item).getMaxStackSize(); } catch (Exception e) { return 64; }
    }
    public static Item func_77642_a(Item item, Item container) { return setContainerItem(item, container); }
    public static int func_150891_b(Item item) {
        return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
    }
    public static String func_77658_a(Item item) {
        return item instanceof LegacyItem ? ((LegacyItem) item).func_77658_a() : item.getDescriptionId();
    }

    // === 繝ｦ繝ｼ繝・ぅ繝ｪ繝・ぅ ===

    private static Field findField(Class<?> clazz, String... names) {
        Class<?> current = clazz;
        while (current != null) {
            for (String name : names) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException e) {
                    // 谺｡繧定ｩｦ縺・
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
