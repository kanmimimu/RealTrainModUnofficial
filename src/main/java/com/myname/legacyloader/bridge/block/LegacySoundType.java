package com.myname.legacyloader.bridge.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public class LegacySoundType extends SoundType {

    public LegacySoundType(float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound, SoundEvent hitSound, SoundEvent fallSound) {
        super(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
    }

    // 1.7.10縺ｮ繧ｳ繝ｳ繧ｹ繝医Λ繧ｯ繧ｿ莠呈鋤 (String name, float volume, float pitch)
    // 螳滄圀縺ｫ縺ｯ蜷榊燕縺ｯ菴ｿ繧上ｌ縺ｪ縺・◆繧∫┌隕・
    public LegacySoundType(String name, float volume, float pitch) {
        this(volume, pitch, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);
    }

    // ==========================================
    //           1.7.10 螳壽焚 (SRG蜷榊ｯｾ蠢・
    // ==========================================
    public static final LegacySoundType WOOD = new LegacySoundType(1.0F, 1.0F, SoundEvents.WOOD_BREAK, SoundEvents.WOOD_STEP, SoundEvents.WOOD_PLACE, SoundEvents.WOOD_HIT, SoundEvents.WOOD_FALL);
    public static final LegacySoundType GROUND = new LegacySoundType(1.0F, 1.0F, SoundEvents.GRAVEL_BREAK, SoundEvents.GRAVEL_STEP, SoundEvents.GRAVEL_PLACE, SoundEvents.GRAVEL_HIT, SoundEvents.GRAVEL_FALL);
    public static final LegacySoundType PLANT = new LegacySoundType(1.0F, 1.0F, SoundEvents.GRASS_BREAK, SoundEvents.GRASS_STEP, SoundEvents.GRASS_PLACE, SoundEvents.GRASS_HIT, SoundEvents.GRASS_FALL);
    public static final LegacySoundType PISTON = new LegacySoundType(1.0F, 1.0F, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);
    public static final LegacySoundType STONE = new LegacySoundType(1.0F, 1.0F, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT, SoundEvents.STONE_FALL);
    public static final LegacySoundType METAL = new LegacySoundType(1.0F, 1.5F, SoundEvents.METAL_BREAK, SoundEvents.METAL_STEP, SoundEvents.METAL_PLACE, SoundEvents.METAL_HIT, SoundEvents.METAL_FALL);
    public static final LegacySoundType GLASS = new LegacySoundType(1.0F, 1.0F, SoundEvents.GLASS_BREAK, SoundEvents.GLASS_STEP, SoundEvents.GLASS_PLACE, SoundEvents.GLASS_HIT, SoundEvents.GLASS_FALL);
    public static final LegacySoundType CLOTH = new LegacySoundType(1.0F, 1.0F, SoundEvents.WOOL_BREAK, SoundEvents.WOOL_STEP, SoundEvents.WOOL_PLACE, SoundEvents.WOOL_HIT, SoundEvents.WOOL_FALL);
    public static final LegacySoundType SAND = new LegacySoundType(1.0F, 1.0F, SoundEvents.SAND_BREAK, SoundEvents.SAND_STEP, SoundEvents.SAND_PLACE, SoundEvents.SAND_HIT, SoundEvents.SAND_FALL);
    public static final LegacySoundType SNOW = new LegacySoundType(1.0F, 1.0F, SoundEvents.SNOW_BREAK, SoundEvents.SNOW_STEP, SoundEvents.SNOW_PLACE, SoundEvents.SNOW_HIT, SoundEvents.SNOW_FALL);
    public static final LegacySoundType LADDER = new LegacySoundType(1.0F, 1.0F, SoundEvents.LADDER_BREAK, SoundEvents.LADDER_STEP, SoundEvents.LADDER_PLACE, SoundEvents.LADDER_HIT, SoundEvents.LADDER_FALL);
    public static final LegacySoundType ANVIL = new LegacySoundType(0.3F, 1.0F, SoundEvents.ANVIL_BREAK, SoundEvents.ANVIL_STEP, SoundEvents.ANVIL_PLACE, SoundEvents.ANVIL_HIT, SoundEvents.ANVIL_FALL);

    // 1.7.10 SRG 繝輔ぅ繝ｼ繝ｫ繝牙錐
    public static final LegacySoundType field_149769_e = STONE;
    public static final LegacySoundType field_149766_f = WOOD;
    public static final LegacySoundType field_149767_g = GROUND;
    public static final LegacySoundType field_149779_h = PLANT;
    public static final LegacySoundType field_149780_i = PISTON;
    public static final LegacySoundType field_149777_j = METAL;
    public static final LegacySoundType field_149778_k = GLASS;
    public static final LegacySoundType field_149775_l = CLOTH;
    public static final LegacySoundType field_149776_m = SAND;
    public static final LegacySoundType field_149773_n = SNOW;
    public static final LegacySoundType field_149774_o = LADDER;
    public static final LegacySoundType field_149772_a = ANVIL;
    public static final LegacySoundType field_149788_p = STONE;

    public String func_150496_b() {
        return "minecraft:block.stone.place";
    }

    public float func_150497_c() {
        return getVolume();
    }

    public float func_150494_d() {
        return getPitch();
    }
}
