package com.myname.legacyloader.bridge.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class LegacyEntityItem extends ItemEntity {
    public LegacyEntityItem(Level level) {
        super(EntityType.ITEM, level);
        setItem(new ItemStack(Items.AIR));
    }

    public LegacyEntityItem(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack == null ? new ItemStack(Items.AIR) : stack);
    }

    public void func_92058_a(ItemStack stack) {
        setItem(stack == null ? new ItemStack(Items.AIR) : stack);
    }

    public ItemStack func_92059_d() {
        return getItem();
    }
}
