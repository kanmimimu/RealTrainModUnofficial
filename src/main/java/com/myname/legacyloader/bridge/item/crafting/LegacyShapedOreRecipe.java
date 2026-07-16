package com.myname.legacyloader.bridge.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

public class LegacyShapedOreRecipe implements LegacyRecipe {
    public final ItemStack output;
    public final Object[] params;
    // rawResult stores Block/Item for deferred resolution when asItem() returns AIR at preInit
    public final Object rawResult;

    public LegacyShapedOreRecipe(Block result, Object... recipe) {
        this.rawResult = result;
        ItemStack stack = (result != null && result.asItem() != Items.AIR)
                ? new ItemStack(result) : ItemStack.EMPTY;
        this.output = stack;
        this.params = recipe;
    }

    public LegacyShapedOreRecipe(Item result, Object... recipe) {
        this.rawResult = result;
        ItemStack stack = (result != null && result != Items.AIR)
                ? new ItemStack(result) : ItemStack.EMPTY;
        this.output = stack;
        this.params = recipe;
    }

    public LegacyShapedOreRecipe(ItemStack result, Object... recipe) {
        this.rawResult = null;
        this.output = result == null ? ItemStack.EMPTY : result.copy();
        this.params = recipe;
    }

    @Override public boolean matches(Object inventoryCrafting, Object world) { return false; }
    @Override public ItemStack getCraftingResult(Object inventoryCrafting) { return output.copy(); }
    @Override public int getRecipeSize() { return 9; }
    @Override public ItemStack getRecipeOutput() { return output; }
}
