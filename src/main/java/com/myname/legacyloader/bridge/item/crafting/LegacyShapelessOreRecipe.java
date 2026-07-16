package com.myname.legacyloader.bridge.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

public class LegacyShapelessOreRecipe implements LegacyRecipe {
    public final ItemStack output;
    public final Object[] params;
    public final Object rawResult;

    public LegacyShapelessOreRecipe(Block result, Object... recipe) {
        this.rawResult = result;
        ItemStack stack = (result != null && result.asItem() != Items.AIR)
                ? new ItemStack(result) : ItemStack.EMPTY;
        this.output = stack;
        this.params = recipe;
    }

    public LegacyShapelessOreRecipe(Item result, Object... recipe) {
        this.rawResult = result;
        ItemStack stack = (result != null && result != Items.AIR)
                ? new ItemStack(result) : ItemStack.EMPTY;
        this.output = stack;
        this.params = recipe;
    }

    public LegacyShapelessOreRecipe(ItemStack result, Object... recipe) {
        this.rawResult = null;
        this.output = result == null ? ItemStack.EMPTY : result.copy();
        this.params = recipe;
    }

    @Override public boolean matches(Object inventoryCrafting, Object world) { return false; }
    @Override public ItemStack getCraftingResult(Object inventoryCrafting) { return output.copy(); }
    @Override public int getRecipeSize() { return params == null ? 0 : params.length; }
    @Override public ItemStack getRecipeOutput() { return output; }
}
