package com.myname.legacyloader.bridge.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LegacyArmorMaterial {
    private final String name;
    private final int maxDamageFactor;
    private final int[] damageReductionAmountArray;
    private final int enchantability;
    public Item customCraftingMaterial = Items.AIR;

    public LegacyArmorMaterial(String name, int maxDamageFactor, int[] reductionAmounts, int enchantability) {
        this.name = name.toLowerCase();
        this.maxDamageFactor = maxDamageFactor;
        this.damageReductionAmountArray = reductionAmounts;
        this.enchantability = enchantability;
    }

    public Holder<ArmorMaterial> asHolder() {
        Supplier<Ingredient> repair = this::getRepairIngredient;
        String modid = com.myname.legacyloader.LegacyLoaderMod.CURRENT_LOADING_MODID;
        if (modid == null || modid.equals("unknown")) modid = "legacy_mod";
        String layerName = normalizeArmorLayerName(name);
        ArmorMaterial material = new ArmorMaterial(
                Map.of(
                        ArmorItem.Type.BOOTS, getDefenseForType(ArmorItem.Type.BOOTS),
                        ArmorItem.Type.LEGGINGS, getDefenseForType(ArmorItem.Type.LEGGINGS),
                        ArmorItem.Type.CHESTPLATE, getDefenseForType(ArmorItem.Type.CHESTPLATE),
                        ArmorItem.Type.HELMET, getDefenseForType(ArmorItem.Type.HELMET)
                ),
                enchantability,
                SoundEvents.ARMOR_EQUIP_GENERIC,
                repair,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(modid, layerName))),
                0.0F,
                0.0F
        );
        return Holder.direct(material);
    }

    public int getDurabilityForType(ArmorItem.Type type) {
        int[] maxDamageArray = new int[]{13, 15, 16, 11};
        int slotIndex = switch (type) {
            case BOOTS -> 0;
            case LEGGINGS -> 1;
            case CHESTPLATE -> 2;
            case HELMET -> 3;
            case BODY -> 2;
        };
        return maxDamageArray[Math.min(slotIndex, maxDamageArray.length - 1)] * this.maxDamageFactor;
    }

    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> getReduction(0);
            case CHESTPLATE, BODY -> getReduction(1);
            case LEGGINGS -> getReduction(2);
            case BOOTS -> getReduction(3);
        };
    }

    private int getReduction(int index) {
        return index >= 0 && index < damageReductionAmountArray.length ? damageReductionAmountArray[index] : 0;
    }

    private static String normalizeArmorLayerName(String name) {
        if (name == null) return "legacy";
        String normalized = name.toLowerCase();
        if (normalized.endsWith("_armor")) {
            normalized = normalized.substring(0, normalized.length() - "_armor".length());
        } else if (normalized.endsWith("armor")) {
            normalized = normalized.substring(0, normalized.length() - "armor".length());
        }
        if (normalized.isEmpty()) return name.toLowerCase();
        return normalized;
    }

    public int getEnchantmentValue() {
        return this.enchantability;
    }

    public Ingredient getRepairIngredient() {
        if (this.customCraftingMaterial != null && this.customCraftingMaterial != Items.AIR) {
            return Ingredient.of(this.customCraftingMaterial);
        }
        return Ingredient.EMPTY;
    }

    public String getName() {
        return this.name;
    }

    public float getToughness() {
        return 0.0F;
    }

    public float getKnockbackResistance() {
        return 0.0F;
    }

    public String name() { return this.name; }
    public int ordinal() { return 0; }
}
