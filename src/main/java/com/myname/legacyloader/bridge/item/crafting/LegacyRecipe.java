package com.myname.legacyloader.bridge.item.crafting;

import net.minecraft.world.item.ItemStack;

// 1.7.10 IRecipe 縺ｮ莉｣逕ｨ繧､繝ｳ繧ｿ繝ｼ繝輔ぉ繝ｼ繧ｹ
public interface LegacyRecipe {
    boolean matches(Object inventoryCrafting, Object world);
    ItemStack getCraftingResult(Object inventoryCrafting);
    int getRecipeSize();
    ItemStack getRecipeOutput();
}