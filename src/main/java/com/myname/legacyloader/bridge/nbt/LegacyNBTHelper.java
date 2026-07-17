package com.myname.legacyloader.bridge.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * 1.7.10 SRG method names for NBTTagCompound (mapped to CompoundTag) and NBTTagList (ListTag).
 * The ClassTransformer redirects invokevirtual calls on these classes to static methods here.
 */
public class LegacyNBTHelper {

    // ===== CompoundTag writes (put*) =====

    public static void func_74774_a(CompoundTag tag, String key, byte value) { tag.putByte(key, value); }
    public static void func_74777_a(CompoundTag tag, String key, short value) { tag.putShort(key, value); }
    public static void func_74783_a(CompoundTag tag, String key, int value)   { tag.putInt(key, value); }
    public static void func_74780_a(CompoundTag tag, String key, long value)  { tag.putLong(key, value); }
    public static void func_74779_a(CompoundTag tag, String key, float value) { tag.putFloat(key, value); }
    public static void func_74757_a(CompoundTag tag, String key, double value)  { tag.putDouble(key, value); }
    public static void func_74757_a(CompoundTag tag, String key, boolean value) { tag.putBoolean(key, value); }
    public static void func_74781_a(CompoundTag tag, String key, String value){ tag.putString(key, value); }
    public static void func_74778_a(CompoundTag tag, String key, String value){ tag.putString(key, value); }
    public static void func_74768_a(CompoundTag tag, String key, boolean value){ tag.putBoolean(key, value); }
    public static void func_74773_a(CompoundTag tag, String key, byte[] value){ tag.putByteArray(key, value); }
    public static void func_74776_a(CompoundTag tag, String key, int[] value) { tag.putIntArray(key, value); }
    public static void func_74782_a(CompoundTag tag, String key, Tag value)   { if (value != null) tag.put(key, value); }
    public static void func_74775_a(CompoundTag tag, String key, ListTag value){ if (value != null) tag.put(key, value); }

    // ===== CompoundTag reads (get*) =====

    public static byte    func_74762_e(CompoundTag tag, String key) { return tag.getByte(key); }
    public static short   func_74766_f(CompoundTag tag, String key) { return tag.getShort(key); }
    public static int     func_74771_a(CompoundTag tag, String key) { return tag.getInt(key); }
    public static long    func_74769_d(CompoundTag tag, String key) { return tag.getLong(key); }
    public static float   func_74760_g(CompoundTag tag, String key) { return tag.getFloat(key); }
    public static double  func_74763_f(CompoundTag tag, String key) { return tag.getDouble(key); }
    public static String  func_74737_b(CompoundTag tag, String key) { return tag.getString(key); }
    public static boolean func_74767_n(CompoundTag tag, String key) { return tag.getBoolean(key); }
    public static byte[]  func_74770_e(CompoundTag tag, String key) { return tag.getByteArray(key); }
    public static int[]   func_74759_k(CompoundTag tag, String key) { return tag.getIntArray(key); }

    public static Tag func_74781_b(CompoundTag tag, String key) { return tag.get(key); }

    public static CompoundTag func_74775_b(CompoundTag tag, String key) { return tag.getCompound(key); }

    public static ListTag func_150295_c(CompoundTag tag, String key, int type) { return tag.getList(key, type); }

    public static boolean func_74764_b(CompoundTag tag, String key) { return tag.contains(key); }

    public static boolean func_150297_b(CompoundTag tag, String key, int type) { return tag.contains(key, type); }

    public static void func_74782_a(CompoundTag tag, CompoundTag other) {
        for (String key : other.getAllKeys()) {
            Tag value = other.get(key);
            if (value != null) tag.put(key, value.copy());
        }
    }

    // ===== ListTag operations =====

    public static void func_74742_a(ListTag list, Tag element) { if (element != null) list.add(element); }

    public static CompoundTag func_150305_b(ListTag list, int index) {
        return list.getCompound(index);
    }

    public static String func_150307_f(ListTag list, int index) {
        return list.getString(index);
    }

    public static int func_74745_c(ListTag list) { return list.size(); }

    public static Tag func_150306_c(ListTag list, int index) {
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }
}
