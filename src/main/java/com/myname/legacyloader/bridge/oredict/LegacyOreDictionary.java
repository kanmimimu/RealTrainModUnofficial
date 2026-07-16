package com.myname.legacyloader.bridge.oredict;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class LegacyOreDictionary {
    public static final int WILDCARD_VALUE = Short.MAX_VALUE;

    private static final Map<String, List<ItemStack>> ORE_REGISTRY = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();
    private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
    private static int nextId = 1;

    public static void registerOre(String name, Item item) {
        if (item != null) registerOre(name, new ItemStack(item));
    }

    public static void registerOre(String name, Block block) {
        if (block != null) registerOre(name, new ItemStack(block));
    }

    public static void registerOre(String name, ItemStack stack) {
        if (name == null || stack == null || stack.isEmpty()) return;
        ORE_REGISTRY.computeIfAbsent(name, k -> new ArrayList<>()).add(stack.copy());
        if (!NAME_TO_ID.containsKey(name)) {
            int id = nextId++;
            NAME_TO_ID.put(name, id);
            ID_TO_NAME.put(id, name);
        }
        System.out.println("LegacyLoader: OreDict registered: " + name + " -> " + stack);
    }

    public static List<ItemStack> getOres(String name) {
        return ORE_REGISTRY.getOrDefault(name, Collections.emptyList());
    }

    public static int getOreID(String name) {
        return NAME_TO_ID.getOrDefault(name, -1);
    }

    public static String getOreName(int id) {
        return ID_TO_NAME.getOrDefault(id, "Unknown");
    }

    public static String[] getOreNames() {
        return ORE_REGISTRY.keySet().toArray(new String[0]);
    }

    public static boolean containsMatch(boolean strict, ItemStack[] inputs, ItemStack... targets) {
        for (ItemStack input : inputs) {
            for (ItemStack target : targets) {
                if (itemMatches(target, input, strict)) return true;
            }
        }
        return false;
    }

    public static boolean itemMatches(ItemStack target, ItemStack input, boolean strict) {
        if (input == null || input.isEmpty() || target == null || target.isEmpty()) return false;
        if (target.getItem() != input.getItem()) return false;
        return !strict || target.getDamageValue() == WILDCARD_VALUE
                || target.getDamageValue() == input.getDamageValue();
    }

    public static void clear() {
        ORE_REGISTRY.clear();
        NAME_TO_ID.clear();
        ID_TO_NAME.clear();
        nextId = 1;
    }
}
