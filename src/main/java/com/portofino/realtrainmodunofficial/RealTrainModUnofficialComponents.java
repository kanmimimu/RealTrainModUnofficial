package com.portofino.realtrainmodunofficial;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * 1.20.1 には DataComponents が無いため、旧 1.20.5+ DataComponent 相当のデータを ItemStack の NBT に
 * 保存する compat レイヤ。キー定数と型付きアクセサ (String / CompoundTag) を提供する。
 * {@code null} 値の set は該当キーの削除として扱う。
 */
public final class RealTrainModUnofficialComponents {
    public static final String SELECTED_MODEL_ID = "selected_model_id";
    public static final String SELECTED_MODEL_DATA_MAP = "selected_model_data_map";
    public static final String RAIL_PREVIEW_START = "rail_preview_start";
    public static final String TRAIN_FORMATION = "train_formation";
    public static final String WIRE_PLACEMENT_START = "wire_placement_start";

    private RealTrainModUnofficialComponents() {
    }

    public static String getString(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : null;
    }

    public static String getStringOrDefault(ItemStack stack, String key, String fallback) {
        String value = getString(stack, key);
        return value != null ? value : fallback;
    }

    public static void setString(ItemStack stack, String key, String value) {
        if (value == null) {
            removeKey(stack, key);
        } else {
            stack.getOrCreateTag().putString(key, value);
        }
    }

    public static CompoundTag getTag(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : null;
    }

    public static void setTag(ItemStack stack, String key, CompoundTag value) {
        if (value == null) {
            removeKey(stack, key);
        } else {
            stack.getOrCreateTag().put(key, value);
        }
    }

    public static void removeKey(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(key);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}
