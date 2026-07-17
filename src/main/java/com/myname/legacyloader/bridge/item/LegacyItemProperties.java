package com.myname.legacyloader.bridge.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class LegacyItemProperties {
    // 譛ｬ迚ｩ縺ｮ繝励Ο繝代ユ繧｣繧剃ｿ晄戟
    private final Item.Properties realProps;

    public LegacyItemProperties() {
        this.realProps = new Item.Properties();
    }

    public Item.Properties getRealProps() {
        return realProps;
    }

    // --- 1.16.5 縺ｮ繝｡繧ｽ繝・ラ蜀咲樟 ---

    // 繧ｿ繝冶ｨｭ螳・(1.20.1縺ｧ縺ｯ辟｡隕・
    public LegacyItemProperties group(LegacyItemGroup group) {
        return this;
    }

    public LegacyItemProperties tab(LegacyItemGroup group) {
        return this;
    }

    // 繧ｹ繧ｿ繝・け謨ｰ
    public LegacyItemProperties maxStackSize(int maxStackSize) {
        realProps.stacksTo(maxStackSize);
        return this;
    }

    public LegacyItemProperties stacksTo(int maxStackSize) {
        realProps.stacksTo(maxStackSize);
        return this;
    }

    // 閠蝉ｹ・､
    public LegacyItemProperties defaultMaxDamage(int maxDamage) {
        realProps.durability(maxDamage);
        return this;
    }

    public LegacyItemProperties durability(int durability) {
        realProps.durability(durability);
        return this;
    }

    // 荳咲㏍諤ｧ
    public LegacyItemProperties isImmuneToFire() {
        realProps.fireResistant();
        return this;
    }

    public LegacyItemProperties fireResistant() {
        realProps.fireResistant();
        return this;
    }

    // 鬟溘∋迚ｩ
    public LegacyItemProperties food(Object food) {
        if (food instanceof FoodProperties) {
            realProps.food((FoodProperties) food);
        }
        return this;
    }

    // --- SRG蜷搾ｼ亥・驛ｨ蜷搾ｼ峨お繧､繝ｪ繧｢繧ｹ ---
    // Mod縺悟・驛ｨ蜷阪〒蜻ｼ繧薙〒縺阪◆蝣ｴ蜷医・蟇ｾ遲・

    // 笘・ｻ雁屓縺ｮ繧ｨ繝ｩ繝ｼ菫ｮ豁｣: func_200916_a -> group
    public LegacyItemProperties func_200916_a(LegacyItemGroup group) {
        return group(group);
    }

    // 莉悶・蜿ｯ閭ｽ諤ｧ縺ｮ縺ゅｋSRG蜷阪ｂ蠢ｵ縺ｮ縺溘ａ螳夂ｾｩ

    // func_200917_a -> maxStackSize
    public LegacyItemProperties func_200917_a(int size) {
        return maxStackSize(size);
    }

    // func_200918_c -> durability
    public LegacyItemProperties func_200918_c(int durability) {
        return durability(durability);
    }

    // func_234689_a_ -> fireResistant (isImmuneToFire)
    public LegacyItemProperties func_234689_a_() {
        return isImmuneToFire();
    }
}