package com.myname.legacyloader.bridge.item.crafting;

import com.myname.legacyloader.LegacyLoaderMod;
import com.myname.legacyloader.bridge.oredict.LegacyOreDictionary;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LegacyRecipeManager {

    private static final List<Object[]> PENDING_SHAPED     = new ArrayList<>();
    private static final List<Object[]> PENDING_SHAPELESS  = new ArrayList<>();
    private static final List<Object[]> PENDING_SMELTING   = new ArrayList<>();
    private static final List<LegacyRecipe> PENDING_ORE_RECIPES = new ArrayList<>();

    private static final AtomicInteger recipeCounter = new AtomicInteger(0);

    public static void addShaped(ItemStack output, Object... params) {
        if (output != null && !output.isEmpty()) {
            PENDING_SHAPED.add(new Object[]{output.copy(), params});
        }
    }

    public static void addShapeless(ItemStack output, Object... params) {
        if (output != null && !output.isEmpty()) {
            PENDING_SHAPELESS.add(new Object[]{output.copy(), params});
        }
    }

    public static void addSmelting(Object input, ItemStack output, float xp) {
        if (output != null && !output.isEmpty()) {
            PENDING_SMELTING.add(new Object[]{input, output.copy(), xp});
        }
    }

    public static void addOreRecipe(LegacyRecipe recipe) {
        if (recipe != null) {
            // Keep even if output is empty — Block-based recipes may resolve later via rawResult
            PENDING_ORE_RECIPES.add(recipe);
        }
    }

    public static void clear() {
        PENDING_SHAPED.clear();
        PENDING_SHAPELESS.clear();
        PENDING_SMELTING.clear();
        PENDING_ORE_RECIPES.clear();
        recipeCounter.set(0);
    }

    public static void injectInto(RecipeManager mgr) {
        try {
            List<RecipeHolder<?>> mergedRecipes = new ArrayList<>(mgr.getRecipes());

            int shapedCount = 0, shapelessCount = 0, smeltingCount = 0;

            for (Object[] entry : PENDING_SHAPED) {
                ItemStack output = (ItemStack) entry[0];
                Object[] params  = (Object[]) entry[1];
                ShapedRecipe recipe = parseShaped(output, params);
                if (recipe != null) {
                    ResourceLocation id = makeId("shaped");
                    mergedRecipes.add(new RecipeHolder<>(id, recipe));
                    shapedCount++;
                }
            }

            for (Object[] entry : PENDING_SHAPELESS) {
                ItemStack output = (ItemStack) entry[0];
                Object[] params  = (Object[]) entry[1];
                ShapelessRecipe recipe = parseShapeless(output, params);
                if (recipe != null) {
                    ResourceLocation id = makeId("shapeless");
                    mergedRecipes.add(new RecipeHolder<>(id, recipe));
                    shapelessCount++;
                }
            }

            for (LegacyRecipe ore : PENDING_ORE_RECIPES) {
                Recipe<?> recipe = null;
                if (ore instanceof LegacyShapedOreRecipe s) {
                    ItemStack out = resolveOutput(s.output, s.rawResult);
                    if (!out.isEmpty()) recipe = parseShaped(out, s.params);
                } else if (ore instanceof LegacyShapelessOreRecipe s) {
                    ItemStack out = resolveOutput(s.output, s.rawResult);
                    if (!out.isEmpty()) recipe = parseShapeless(out, s.params);
                }
                if (recipe != null) {
                    ResourceLocation id = makeId("ore");
                    mergedRecipes.add(new RecipeHolder<>(id, recipe));
                    shapedCount++;
                }
            }

            for (Object[] entry : PENDING_SMELTING) {
                Object input     = entry[0];
                ItemStack output = (ItemStack) entry[1];
                float xp         = (float) entry[2];
                SmeltingRecipe recipe = parseSmelting(input, output, xp);
                if (recipe != null) {
                    ResourceLocation id = makeId("smelting");
                    mergedRecipes.add(new RecipeHolder<>(id, recipe));
                    smeltingCount++;
                }
            }

            // Final safety: strip any recipe whose result encodes as empty
            mergedRecipes.removeIf(holder -> {
                try {
                    ItemStack result = holder.value().getResultItem(null);
                    return result == null || result.isEmpty();
                } catch (Exception e) {
                    return true; // remove broken recipes
                }
            });

            mgr.replaceRecipes(mergedRecipes);

            LegacyLoaderMod.LOGGER.info("LegacyLoader: Injected " + shapedCount + " shaped, " +
                    shapelessCount + " shapeless, " + smeltingCount + " smelting recipes");

        } catch (Exception e) {
            System.err.println("LegacyLoader: Failed to inject recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ItemStack resolveOutput(ItemStack stored, Object rawResult) {
        if (!stored.isEmpty()) return stored;
        if (rawResult instanceof Block b) {
            Item item = b.asItem();
            return (item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY;
        }
        if (rawResult instanceof Item item) {
            return (item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    private static Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {}
        }
        // Search all declared fields as fallback
        for (Field f : clazz.getDeclaredFields()) {
            Class<?> type = f.getType();
            if (Map.class.isAssignableFrom(type)) {
                f.setAccessible(true);
                try {
                    Object val = f.get(null);
                    if (val == null) return f; // static map field candidate
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    @SuppressWarnings("removal")
    private static ResourceLocation makeId(String prefix) {
        return ResourceLocation.fromNamespaceAndPath("legacyloader", prefix + "_" + recipeCounter.getAndIncrement());
    }

    // 1.7.10蠖｢蠑・ ("ABC", "DEF", 'A', item, 'B', item2, ...) 繧偵ヱ繝ｼ繧ｹ
    private static ShapedRecipe parseShaped(ItemStack output, Object[] params) {
        if (output == null || output.isEmpty()) return null;
        try {
            List<String> rows = new ArrayList<>();
            Map<Character, Ingredient> keyMap = new HashMap<>();
            int i = 0;

            while (i < params.length && params[i] instanceof String) {
                rows.add((String) params[i++]);
            }
            if (rows.isEmpty()) return null;

            while (i < params.length - 1) {
                Object key = params[i++];
                Object val = params[i++];
                char c;
                if (key instanceof Character) c = (Character) key;
                else if (key instanceof String && ((String) key).length() == 1) c = ((String) key).charAt(0);
                else continue;
                Ingredient ing = toIngredient(val);
                if (ing != Ingredient.EMPTY) keyMap.put(c, ing);
            }

            int width  = rows.stream().mapToInt(String::length).max().orElse(0);
            int height = rows.size();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (int row = 0; row < rows.size(); row++) {
                String line = rows.get(row);
                for (int col = 0; col < line.length(); col++) {
                    char c = line.charAt(col);
                    if (c == ' ') continue;
                    Ingredient ing = keyMap.get(c);
                    if (ing != null) ingredients.set(row * width + col, ing);
                }
            }

            ShapedRecipePattern pattern = new ShapedRecipePattern(width, height, ingredients, Optional.empty());
            return new ShapedRecipe("", CraftingBookCategory.MISC, pattern, output);
        } catch (Exception e) {
            System.err.println("LegacyLoader: Failed to parse shaped recipe: " + e.getMessage());
            return null;
        }
    }

    private static ShapelessRecipe parseShapeless(ItemStack output, Object[] params) {
        if (output == null || output.isEmpty()) return null;
        try {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (Object param : params) {
                Ingredient ing = toIngredient(param);
                if (ing != Ingredient.EMPTY) ingredients.add(ing);
            }
            if (ingredients.isEmpty()) return null;
            return new ShapelessRecipe("", CraftingBookCategory.MISC, output, ingredients);
        } catch (Exception e) {
            System.err.println("LegacyLoader: Failed to parse shapeless recipe: " + e.getMessage());
            return null;
        }
    }

    private static SmeltingRecipe parseSmelting(Object input, ItemStack output, float xp) {
        try {
            Ingredient ing = toIngredient(input);
            if (ing == Ingredient.EMPTY) return null;
            return new SmeltingRecipe("", CookingBookCategory.MISC, ing, output, xp, 200);
        } catch (Exception e) {
            System.err.println("LegacyLoader: Failed to parse smelting recipe: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Ingredient toIngredient(Object val) {
        if (val == null) return Ingredient.EMPTY;
        if (val instanceof Ingredient) return (Ingredient) val;
        if (val instanceof Item)      return Ingredient.of((Item) val);
        if (val instanceof ItemStack) {
            ItemStack stack = (ItemStack) val;
            return stack.isEmpty() ? Ingredient.EMPTY : Ingredient.of(stack);
        }
        if (val instanceof Block) return Ingredient.of(((Block) val).asItem());
        if (val instanceof String) {
            List<ItemStack> ores = LegacyOreDictionary.getOres((String) val);
            if (ores.isEmpty()) return Ingredient.EMPTY;
            return Ingredient.of(ores.stream());
        }
        if (val instanceof Object[]) {
            Object[] arr = (Object[]) val;
            List<ItemStack> stacks = new ArrayList<>();
            for (Object o : arr) {
                Ingredient ing = toIngredient(o);
                if (ing != Ingredient.EMPTY) {
                    stacks.addAll(Arrays.asList(ing.getItems()));
                }
            }
            return stacks.isEmpty() ? Ingredient.EMPTY : Ingredient.of(stacks.toArray(new ItemStack[0]));
        }
        return Ingredient.EMPTY;
    }
}
