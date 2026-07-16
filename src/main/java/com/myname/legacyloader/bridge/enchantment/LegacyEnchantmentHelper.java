package com.myname.legacyloader.bridge.enchantment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public final class LegacyEnchantmentHelper {
    private static final Random ENCHANTMENT_RANDOM = new Random();

    private LegacyEnchantmentHelper() {
    }

    public static int getEnchantmentLevel(int id, Object stack) {
        return 0;
    }

    public static Map<Integer, Integer> getEnchantments(Object stack) {
        return new LinkedHashMap<>();
    }

    public static void setEnchantments(Map<?, ?> enchantments, Object stack) {
    }

    public static int getMaxEnchantmentLevel(int id, Object[] stacks) {
        return 0;
    }

    public static int calcItemStackEnchantability(Random random, int slot, int power, Object stack) {
        Random useRandom = random == null ? ENCHANTMENT_RANDOM : random;
        if (power <= 0) {
            return 0;
        }
        return Math.max(1, useRandom.nextInt(Math.max(1, power)) + 1 + slot);
    }

    public static void func_151384_a(Object user, Object target) {
    }

    public static void func_151385_b(Object user, Object target) {
    }
}
