package com.myname.legacyloader.bridge.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class LegacyItemFood extends LegacyItem {

    public LegacyItemFood(int healAmount, float saturation, boolean isWolfsFavoriteMeat) {
        super(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(healAmount)
                .saturationModifier(saturation)
                .build()));
    }

    public LegacyItemFood(int healAmount, boolean isWolfsFavoriteMeat) {
        this(healAmount, 0.6F, isWolfsFavoriteMeat);
    }

    public Item setAlwaysEdible() {
        return this;
    }

    public Item setPotionEffect(int id, int duration, int amplifier, float probability) {
        return this;
    }

    // SRG蜷・
    public Item func_77848_i() { return setAlwaysEdible(); }
    public Item func_77844_a(int id, int duration, int amplifier, float probability) {
        return setPotionEffect(id, duration, amplifier, probability);
    }
}