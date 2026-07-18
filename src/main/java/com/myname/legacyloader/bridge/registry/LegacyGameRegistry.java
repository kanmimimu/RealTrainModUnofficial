package com.myname.legacyloader.bridge.registry;

import com.myname.legacyloader.LegacyLoaderMod;
import com.myname.legacyloader.bridge.block.*;
import com.myname.legacyloader.bridge.core.RegistryNameHelper;
import com.myname.legacyloader.bridge.fml.IWorldGenerator;
import com.myname.legacyloader.bridge.item.*;
import com.myname.legacyloader.bridge.item.crafting.LegacyIFuelHandler;
import com.myname.legacyloader.bridge.item.crafting.LegacyRecipe;
import com.myname.legacyloader.bridge.item.crafting.LegacyRecipeManager;
import com.myname.legacyloader.bridge.tileentity.LegacyTileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.reflect.Constructor;
import java.util.*;

public class LegacyGameRegistry {

    public static final List<BlockEntry> PENDING_BLOCKS = new ArrayList<>();
    private static final List<ItemEntry> PENDING_ITEMS = new ArrayList<>();
    private static final Map<Item, ResourceLocation> PENDING_ITEM_REGISTRY_NAMES = new LinkedHashMap<>();

    private static final Set<ResourceLocation> REGISTERED_BLOCK_NAMES = new HashSet<>();
    private static final Set<ResourceLocation> REGISTERED_ITEM_NAMES = new HashSet<>();
    private static final Set<Block> REGISTERED_BLOCK_INSTANCES = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Item> REGISTERED_ITEM_INSTANCES = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Block> TRACKED_BLOCKS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Item> TRACKED_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());

    private static final List<LegacyBlockSlab> PENDING_SLABS = new ArrayList<>();
    private static final Map<String, LegacyBlockSlab> SLAB_BY_NAME = new HashMap<>();

    // 笘・ｿｽ蜉: 逋ｻ骭ｲ蜷阪・繧ｫ繧ｦ繝ｳ繧ｿ繝ｼ・磯㍾隍・屓驕ｿ・・
    private static final Map<String, Integer> NAME_COUNTERS = new HashMap<>();

    public static void trackBlock(Block block) {
        if (block != null) {
            TRACKED_BLOCKS.add(block);
        }
    }

    public static void trackSlabBlock(LegacyBlockSlab slab) {
        if (slab != null && !PENDING_SLABS.contains(slab)) {
            trackBlock(slab);
            PENDING_SLABS.add(slab);
        }
    }

    public static void trackItem(Item item) {
        if (item != null) {
            TRACKED_ITEMS.add(item);
        }
    }

    public static Block registerBlock(Block block, String name) {
        return registerBlock(block, null, name);
    }

    public static Block registerBlock(Block block, Class<?> itemClass, String name) {
        if (block == null || name == null) return block;

        if (REGISTERED_BLOCK_INSTANCES.contains(block)) {
            System.out.println("LegacyLoader: Block already registered, skipping: " + name);
            return block;
        }

        String modid = safeNamespace(LegacyLoaderMod.CURRENT_LOADING_MODID);

        String safeName = name.toLowerCase(Locale.ROOT);
        if (safeName.startsWith("tile.")) safeName = safeName.substring(5);

        // 笘・㍾隕・ 蜷榊燕驥崎､・ｒ讀懷・縺励※閾ｪ蜍輔Μ繝阪・繝
        @SuppressWarnings("removal")
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modid, safeName);

        if (REGISTERED_BLOCK_NAMES.contains(rl)) {
            // 驥崎､・錐繧呈､懷・ 竊・繧ｫ繧ｦ繝ｳ繧ｿ繝ｼ縺ｧ荳諢丞喧
            String baseName = safeName;
            int counter = NAME_COUNTERS.getOrDefault(baseName, 1);
            counter++;
            NAME_COUNTERS.put(baseName, counter);
            safeName = baseName + "_" + counter;

            @SuppressWarnings("removal")
            ResourceLocation newRL = ResourceLocation.fromNamespaceAndPath(modid, safeName);
            rl = newRL;

            System.out.println("LegacyLoader: Name conflict detected for '" + baseName +
                    "', renamed to: " + rl);
        }

        RegistryNameHelper.setRegistryName(block, rl);

        if (block instanceof LegacyBlock) {
            ((LegacyBlock) block).markForRegistration();
        }

        if (block instanceof LegacyBlockSlab) {
            LegacyBlockSlab slab = (LegacyBlockSlab) block;
            SLAB_BY_NAME.put(safeName, slab);
        }

        // 笘・ｿｽ蜉: 髫取ｮｵ縺ｮ繧ｽ繝ｼ繧ｹ繝｡繧ｿ繝・・繧ｿ繧定ｨ倬鹸
        if (block instanceof LegacyBlockStairs) {
            LegacyBlockStairs stairs = (LegacyBlockStairs) block;
            LegacyBlock.STAIRS_SOURCE_META.put(rl, stairs.getSourceMetadata());
        }

        PENDING_BLOCKS.add(new BlockEntry(modid, safeName, block, itemClass));
        REGISTERED_BLOCK_NAMES.add(rl);
        REGISTERED_BLOCK_INSTANCES.add(block);

        System.out.println("LegacyLoader: Queued block: " + rl +
                " (" + block.getClass().getSimpleName() + ")");

        return block;
    }

    public static void registerItem(Item item, String name) {
        if (item == null || name == null) return;
        if (REGISTERED_ITEM_INSTANCES.contains(item)) return;

        String modid = safeNamespace(LegacyLoaderMod.CURRENT_LOADING_MODID);
        String safeName = name.toLowerCase(Locale.ROOT);
        if (safeName.startsWith("item.")) safeName = safeName.substring(5);

        @SuppressWarnings("removal")
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modid, safeName);
        RegistryNameHelper.setRegistryName(item, rl);
        if (item instanceof LegacyItem legacyItem
                && legacyItem.legacyTextureName != null
                && !legacyItem.legacyTextureName.isBlank()) {
            LegacyItem.TEXTURE_OVERRIDES.put(rl, cleanTextureName(legacyItem.legacyTextureName));
        }
        PENDING_ITEM_REGISTRY_NAMES.put(item, rl);

        if (!REGISTERED_ITEM_NAMES.contains(rl)) {
            PENDING_ITEMS.add(new ItemEntry(modid, safeName, item));
            REGISTERED_ITEM_NAMES.add(rl);
            REGISTERED_ITEM_INSTANCES.add(item);
        }
    }

    public static void registerCustomItemStack(String name, ItemStack stack) {
    }

    public static Map<Item, ResourceLocation> getPendingItemRegistryNames() {
        return PENDING_ITEM_REGISTRY_NAMES;
    }

    private static void pairSlabs() {
        List<LegacyBlockSlab> singleSlabs = new ArrayList<>();
        List<LegacyBlockSlab> doubleSlabs = new ArrayList<>();

        for (LegacyBlockSlab slab : PENDING_SLABS) {
            if (slab.isDouble) doubleSlabs.add(slab);
            else singleSlabs.add(slab);
        }

        // 蜷榊燕繝吶・繧ｹ縺ｧ繝壹い繝ｪ繝ｳ繧ｰ
        for (Map.Entry<String, LegacyBlockSlab> entry : SLAB_BY_NAME.entrySet()) {
            String name = entry.getKey();
            LegacyBlockSlab slab = entry.getValue();

            if (slab.getPairedSlab() != null) continue;

            if (slab.isDouble) {
                String[] possibleSingleNames = {
                        name.replace("_double", ""),
                        name.replace("double_", ""),
                        name.replace("double", ""),
                        name.replace("doubleslab", "slab"),
                        name.replace("_doubleslab", "_slab"),
                        // 笘・ｿｽ蜉: double_slab1 竊・slab1 繝代ち繝ｼ繝ｳ
                        name.replace("double_slab", "slab")
                };

                for (String singleName : possibleSingleNames) {
                    LegacyBlockSlab single = SLAB_BY_NAME.get(singleName);
                    if (single != null && !single.isDouble) {
                        slab.setPairedSlab(single);
                        break;
                    }
                }
            }
        }

        // 繧ｯ繝ｩ繧ｹ蜷阪・繝ｼ繧ｹ縺ｧ繝壹い繝ｪ繝ｳ繧ｰ
        for (LegacyBlockSlab single : singleSlabs) {
            if (single.getPairedSlab() != null) continue;
            String singleClassName = single.getClass().getSimpleName();

            for (LegacyBlockSlab doubleSlab : doubleSlabs) {
                if (doubleSlab.getPairedSlab() != null) continue;
                String doubleClassName = doubleSlab.getClass().getSimpleName();

                if (doubleClassName.contains("Double") &&
                        doubleClassName.replace("Double", "").equals(singleClassName)) {
                    single.setPairedSlab(doubleSlab);
                    break;
                }
            }
        }

        // 繝槭ユ繝ｪ繧｢繝ｫ繝吶・繧ｹ縺ｧ繝壹い繝ｪ繝ｳ繧ｰ
        for (LegacyBlockSlab single : singleSlabs) {
            if (single.getPairedSlab() != null) continue;
            for (LegacyBlockSlab doubleSlab : doubleSlabs) {
                if (doubleSlab.getPairedSlab() != null) continue;
                if (single.legacyMaterial == doubleSlab.legacyMaterial &&
                        single.getMaxVariants() == doubleSlab.getMaxVariants() &&
                        Objects.equals(single.legacyTextureName, doubleSlab.legacyTextureName)) {
                    single.setPairedSlab(doubleSlab);
                    break;
                }
            }
        }
    }

    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            queueUnregisteredTrackedBlocks();
            pairSlabs();

            for (BlockEntry entry : PENDING_BLOCKS) {
                if (entry.block == null) continue;
                @SuppressWarnings("removal")
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(entry.modid, entry.name);
                try {
                    event.register(Registries.BLOCK, rl, () -> entry.block);
                    System.out.println("LegacyLoader: Registered block: " + rl);
                } catch (Exception e) {
                    System.err.println("LegacyLoader: Failed to register block: " + rl);
                    e.printStackTrace();
                }
            }
        }

        if (event.getRegistryKey().equals(Registries.ITEM)) {
            queueUnregisteredTrackedItems();

            for (BlockEntry entry : PENDING_BLOCKS) {
                if (entry.block == null) continue;

                @SuppressWarnings("removal")
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(entry.modid, entry.name);

                Item itemBlock = null;

                // 繧ｹ繝ｩ繝・
                if (entry.block instanceof LegacyBlockSlab) {
                    LegacyBlockSlab slab = (LegacyBlockSlab) entry.block;
                    if (slab.isDouble) {
                        // 笘・ム繝悶Ν繧ｹ繝ｩ繝悶ｂ繧｢繧､繝・Β繧堤匳骭ｲ縺吶ｋ・郁ｨｭ鄂ｮ縺ｯ縺励↑縺・′蜿ら・縺ｫ蠢・ｦ・ｼ・
                        itemBlock = new LegacyBlockItem(entry.block);
                    } else {
                        LegacyBlockSlab doubleSlab = slab.getPairedSlab();
                        itemBlock = new LegacySlabBlockItem(slab, doubleSlab);
                    }
                }
                // 繧ｫ繧ｹ繧ｿ繝ItemBlock繧ｯ繝ｩ繧ｹ
                else if (entry.itemClass != null) {
                    try {
                        for (Constructor<?> c : entry.itemClass.getConstructors()) {
                            Class<?>[] paramTypes = c.getParameterTypes();
                            if (paramTypes.length == 1 && Block.class.isAssignableFrom(paramTypes[0])) {
                                itemBlock = (Item) c.newInstance(entry.block);
                                break;
                            } else if (paramTypes.length == 2 &&
                                    Block.class.isAssignableFrom(paramTypes[0]) &&
                                    Block.class.isAssignableFrom(paramTypes[1])) {
                                itemBlock = (Item) c.newInstance(entry.block, entry.block);
                                break;
                            }
                        }
                        if (itemBlock instanceof LegacyBlockItem) {
                            ((LegacyBlockItem) itemBlock).setHasSubtypes(true);
                        }
                    } catch (Exception e) {
                        System.err.println("LegacyLoader: Failed to create custom ItemBlock for " + rl);
                        e.printStackTrace();
                    }
                }

                // 繝・ヵ繧ｩ繝ｫ繝・
                if (itemBlock == null) {
                    if (entry.block instanceof LegacyMetadataBlock) {
                        itemBlock = new LegacyMetadataBlockItem(entry.block);
                    } else if (entry.block instanceof ILegacyBlock) {
                        ILegacyBlock lb = (ILegacyBlock) entry.block;
                        if (lb.hasSubtypes() || lb.getMaxMetadata() > 1) {
                            itemBlock = new LegacyMetadataBlockItem(entry.block);
                        } else {
                            itemBlock = new LegacyBlockItem(entry.block);
                        }
                    } else {
                        itemBlock = new LegacyBlockItem(entry.block);
                    }
                }

                // 繧ｯ繝ｪ繧ｨ繧､繝・ぅ繝悶ち繝・
                if (entry.block instanceof LegacyBlock) {
                    LegacyCreativeTab tab = ((LegacyBlock) entry.block).getCreativeTab();
                    if (tab != null) tab.addBlock(entry.block);
                } else if (entry.block instanceof LegacyBlockSlab) {
                    LegacyBlockSlab slab = (LegacyBlockSlab) entry.block;
                    LegacyCreativeTab tab = slab.getCreativeTab();
                    if (tab != null && !slab.isDouble) tab.addBlock(entry.block);
                } else if (entry.block instanceof LegacyBlockStairs) {
                    LegacyCreativeTab tab = ((LegacyBlockStairs) entry.block).getCreativeTab();
                    if (tab != null) tab.addBlock(entry.block);
                }

                final Item finalItem = itemBlock;
                RegistryNameHelper.setRegistryName(finalItem, rl);
                try {
                    event.register(Registries.ITEM, rl, () -> finalItem);
                    REGISTERED_ITEM_INSTANCES.add(finalItem);
                } catch (Exception e) {
                    System.err.println("LegacyLoader: Failed to register item: " + rl);
                    e.printStackTrace();
                }
            }

            queueUnregisteredTrackedItems();

            for (ItemEntry entry : PENDING_ITEMS) {
                @SuppressWarnings("removal")
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(entry.modid, entry.name);
                try {
                    event.register(Registries.ITEM, rl, () -> entry.item);
                    REGISTERED_ITEM_INSTANCES.add(entry.item);
                } catch (Exception e) {
                    System.err.println("LegacyLoader: Failed to register item: " + rl);
                    e.printStackTrace();
                }
            }
        }
    }

    private static void queueUnregisteredTrackedBlocks() {
        for (Block block : new ArrayList<>(TRACKED_BLOCKS)) {
            if (block == null || REGISTERED_BLOCK_INSTANCES.contains(block)) continue;

            ResourceLocation existing = RegistryNameHelper.getRegistryName(block);
            String modid = existing != null ? existing.getNamespace() : safeNamespace(LegacyLoaderMod.CURRENT_LOADING_MODID);
            String baseName = existing != null ? existing.getPath() : autoName("auto_block", block);
            baseName = sanitizePath(baseName);

            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modid, baseName);
            if (REGISTERED_BLOCK_NAMES.contains(rl)) {
                baseName = nextFreeName(REGISTERED_BLOCK_NAMES, modid, baseName);
                rl = ResourceLocation.fromNamespaceAndPath(modid, baseName);
            }

            RegistryNameHelper.setRegistryName(block, rl);
            PENDING_BLOCKS.add(new BlockEntry(modid, baseName, block, null));
            REGISTERED_BLOCK_NAMES.add(rl);
            REGISTERED_BLOCK_INSTANCES.add(block);
            System.out.println("LegacyLoader: Auto-queued unregistered block: " + rl +
                    " (" + block.getClass().getSimpleName() + ")");
        }
    }

    private static void queueUnregisteredTrackedItems() {
        for (Item item : new ArrayList<>(TRACKED_ITEMS)) {
            if (item == null || REGISTERED_ITEM_INSTANCES.contains(item)) continue;

            ResourceLocation existing = RegistryNameHelper.getRegistryName(item);
            String modid = existing != null ? existing.getNamespace() : safeNamespace(LegacyLoaderMod.CURRENT_LOADING_MODID);
            String baseName = existing != null ? existing.getPath() : autoName("auto_item", item);
            baseName = sanitizePath(baseName);

            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modid, baseName);
            if (REGISTERED_ITEM_NAMES.contains(rl)) {
                baseName = nextFreeName(REGISTERED_ITEM_NAMES, modid, baseName);
                rl = ResourceLocation.fromNamespaceAndPath(modid, baseName);
            }

            RegistryNameHelper.setRegistryName(item, rl);
            PENDING_ITEMS.add(new ItemEntry(modid, baseName, item));
            PENDING_ITEM_REGISTRY_NAMES.put(item, rl);
            REGISTERED_ITEM_NAMES.add(rl);
            REGISTERED_ITEM_INSTANCES.add(item);
            System.out.println("LegacyLoader: Auto-queued unregistered item: " + rl +
                    " (" + item.getClass().getSimpleName() + ")");
        }
    }

    private static String autoName(String prefix, Object value) {
        String legacyName = readStringField(value, "legacyUnlocalizedName");
        if (legacyName != null && !legacyName.isBlank()) {
            return legacyName;
        }
        return prefix + "_" + value.getClass().getSimpleName();
    }

    private static String readStringField(Object value, String fieldName) {
        try {
            java.lang.reflect.Field field = value.getClass().getField(fieldName);
            Object fieldValue = field.get(value);
            return fieldValue instanceof String ? (String) fieldValue : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String sanitizePath(String name) {
        String safe = name == null ? "unnamed" : name;
        if (safe.startsWith("tile.")) safe = safe.substring(5);
        if (safe.startsWith("item.")) safe = safe.substring(5);
        safe = safe.replace(':', '_').toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
        return safe.isBlank() ? "unnamed" : safe;
    }

    private static String cleanTextureName(String name) {
        String safe = name.trim().replace('\\', '/');
        if (safe.startsWith("/")) safe = safe.substring(1);
        if (safe.endsWith(".png")) safe = safe.substring(0, safe.length() - 4);
        if (safe.startsWith("textures/")) safe = safe.substring("textures/".length());
        if (safe.startsWith("items/")) safe = safe.substring("items/".length());
        if (safe.startsWith("item/")) safe = safe.substring("item/".length());
        return safe.toLowerCase(Locale.ROOT);
    }

    private static String nextFreeName(Set<ResourceLocation> usedNames, String modid, String baseName) {
        int counter = NAME_COUNTERS.getOrDefault(baseName, 1);
        String candidate;
        do {
            counter++;
            candidate = baseName + "_" + counter;
        } while (usedNames.contains(ResourceLocation.fromNamespaceAndPath(modid, candidate)));
        NAME_COUNTERS.put(baseName, counter);
        return candidate;
    }

    // ============ 繝ｬ繧ｷ繝・============
    public static void addRecipe(ItemStack output, Object... params) {
        LegacyRecipeManager.addShaped(output, params);
    }

    public static void addRecipe(LegacyRecipe recipe) {
        LegacyRecipeManager.addOreRecipe(recipe);
    }

    public static LegacyRecipe addRecipeAndReturn(LegacyRecipe recipe) {
        LegacyRecipeManager.addOreRecipe(recipe);
        return recipe;
    }

    public static void addShapelessRecipe(ItemStack output, Object... params) {
        LegacyRecipeManager.addShapeless(output, params);
    }

    // 本家 GameRegistry.addShapedRecipe/addShapelessRecipe は IRecipe を返す (戻り値を使う mod がある)。
    // 登録は LegacyRecipeManager に委譲し、最小の LegacyRecipe 実装を返す。
    public static LegacyRecipe addShapedRecipe(final ItemStack output, Object... params) {
        LegacyRecipeManager.addShaped(output, params);
        return newRecipeHandle(output);
    }

    public static LegacyRecipe addShapelessRecipeReturning(final ItemStack output, Object... params) {
        LegacyRecipeManager.addShapeless(output, params);
        return newRecipeHandle(output);
    }

    private static LegacyRecipe newRecipeHandle(final ItemStack output) {
        return new LegacyRecipe() {
            @Override public boolean matches(Object inventoryCrafting, Object world) { return false; }
            @Override public ItemStack getCraftingResult(Object inventoryCrafting) { return output; }
            @Override public int getRecipeSize() { return 0; }
            @Override public ItemStack getRecipeOutput() { return output; }
        };
    }

    public static void addSmelting(Object input, ItemStack output, float xp) {
        LegacyRecipeManager.addSmelting(input, output, xp);
    }

    public static void addSmelting(Block input, ItemStack output, float xp) {
        LegacyRecipeManager.addSmelting(input, output, xp);
    }

    public static void addSmelting(Item input, ItemStack output, float xp) {
        LegacyRecipeManager.addSmelting(input, output, xp);
    }

    public static void addSmelting(ItemStack input, ItemStack output, float xp) {
        LegacyRecipeManager.addSmelting(input, output, xp);
    }

    // ============ 繝ｯ繝ｼ繝ｫ繝臥函謌・============
    private static final List<IWorldGenerator> WORLD_GENERATORS = new ArrayList<>();

    public static void registerWorldGenerator(IWorldGenerator generator, int modGenerationWeight) {
        if (generator != null) {
            WORLD_GENERATORS.add(generator);
            System.out.println("LegacyLoader: Registered world generator: " + generator.getClass().getName());
        }
    }

    public static List<IWorldGenerator> getWorldGenerators() {
        return WORLD_GENERATORS;
    }

    // ============ 辯・侭繝上Φ繝峨Λ繝ｼ ============
    private static final List<LegacyIFuelHandler> FUEL_HANDLERS = new ArrayList<>();

    public static void registerFuelHandler(com.myname.legacyloader.bridge.fml.LegacyIFuelHandler handler) {
        if (handler != null) FUEL_HANDLERS.add(handler::getBurnTime);
    }

    public static void registerFuelHandler(LegacyIFuelHandler handler) {
        if (handler != null) FUEL_HANDLERS.add(handler);
    }

    public static List<LegacyIFuelHandler> getFuelHandlers() {
        return FUEL_HANDLERS;
    }

    @SuppressWarnings("unchecked")
    public static void registerTileEntity(Class<? extends BlockEntity> tileEntityClass, String id) {
        if (tileEntityClass == null || id == null) return;
        if (LegacyTileEntity.class.isAssignableFrom(tileEntityClass)) {
            LegacyTileEntity.addMapping((Class<? extends LegacyTileEntity>) tileEntityClass, id);
        }
    }
    public static void func_149759_a(Class<? extends BlockEntity> tileEntityClass, String id) { registerTileEntity(tileEntityClass, id); }
    public static void registerTileEntityWithAlternatives(Class<? extends BlockEntity> tileEntityClass, String id, String... alternatives) {
        registerTileEntity(tileEntityClass, id);
        if (alternatives != null) {
            for (String alternative : alternatives) {
                registerTileEntity(tileEntityClass, alternative);
            }
        }
    }
    public static void func_152345_a(ItemStack output, Object... params) { addRecipe(output, params); }
    public static void func_152346_a(LegacyRecipe recipe) { addRecipe(recipe); }
    public static void func_152348_a(ItemStack output, Object... params) { addShapelessRecipe(output, params); }

    public static class BlockEntry {
        public final String modid;
        public final String name;
        public final Block block;
        public final Class<?> itemClass;

        public BlockEntry(String modid, String name, Block block, Class<?> itemClass) {
            this.modid = safeNamespace(modid);
            this.name = name;
            this.block = block;
            this.itemClass = itemClass;
        }
    }

    private static class ItemEntry {
        final String modid;
        final String name;
        final Item item;

        ItemEntry(String modid, String name, Item item) {
            this.modid = safeNamespace(modid);
            this.name = name;
            this.item = item;
        }
    }

    public static void clearRegistries() {
        PENDING_BLOCKS.clear();
        PENDING_ITEMS.clear();
        PENDING_ITEM_REGISTRY_NAMES.clear();
        REGISTERED_BLOCK_NAMES.clear();
        REGISTERED_ITEM_NAMES.clear();
        REGISTERED_BLOCK_INSTANCES.clear();
        REGISTERED_ITEM_INSTANCES.clear();
        TRACKED_BLOCKS.clear();
        TRACKED_ITEMS.clear();
        PENDING_SLABS.clear();
        SLAB_BY_NAME.clear();
        NAME_COUNTERS.clear();
        WORLD_GENERATORS.clear();
        FUEL_HANDLERS.clear();
        LegacyRecipeManager.clear();
    }

    private static String safeNamespace(String modid) {
        if (modid == null || modid.equals("unknown")) return "legacy_mod";
        String safe = modid.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        return safe.isEmpty() ? "legacy_mod" : safe;
    }
}
