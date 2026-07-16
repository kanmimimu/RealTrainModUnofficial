package com.myname.legacyloader.bridge.stats;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LegacyAchievement extends LegacyStatBase {
    public ItemStack field_75990_d;
    public final int displayColumn;
    public final int displayRow;
    public final LegacyAchievement parentAchievement;

    public LegacyAchievement(String id, String name, int column, int row, Item item, LegacyAchievement parent) {
        this(id, name, column, row, item == null ? ItemStack.EMPTY : new ItemStack(item), parent);
    }

    public LegacyAchievement(String id, String name, int column, int row, Block block, LegacyAchievement parent) {
        this(id, name, column, row, block == null ? ItemStack.EMPTY : new ItemStack(block), parent);
    }

    public LegacyAchievement(String id, String name, int column, int row, ItemStack stack, LegacyAchievement parent) {
        super(id, name);
        this.displayColumn = column;
        this.displayRow = row;
        this.field_75990_d = stack == null ? ItemStack.EMPTY : stack;
        this.parentAchievement = parent;
    }

    public LegacyAchievement func_75971_g() {
        return this;
    }

    public LegacyAchievement registerStat() {
        return this;
    }
}
