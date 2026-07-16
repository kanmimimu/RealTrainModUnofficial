package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.LegacyLoaderMod;
import com.myname.legacyloader.bridge.block.ILegacyBlock;
import com.myname.legacyloader.bridge.block.LegacyBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LegacyCreativeTab extends CreativeTabs {

    public static LegacyCreativeTab[] creativeTabArray = new LegacyCreativeTab[12];

    public static final List<LegacyCreativeTab> TABS = new ArrayList<>();

    public static final LegacyCreativeTab tabBlock = new LegacyCreativeTab(0, "buildingBlocks");
    public static final LegacyCreativeTab tabDecorations = new LegacyCreativeTab(1, "decorations");
    public static final LegacyCreativeTab tabRedstone = new LegacyCreativeTab(2, "redstone");
    public static final LegacyCreativeTab tabTransport = new LegacyCreativeTab(3, "transportation");
    public static final LegacyCreativeTab tabMisc = new LegacyCreativeTab(4, "misc");
    public static final LegacyCreativeTab tabAllSearch = new LegacyCreativeTab(5, "search");
    public static final LegacyCreativeTab tabFood = new LegacyCreativeTab(6, "food");
    public static final LegacyCreativeTab tabTools = new LegacyCreativeTab(7, "tools");
    public static final LegacyCreativeTab tabCombat = new LegacyCreativeTab(8, "combat");
    public static final LegacyCreativeTab tabBrewing = new LegacyCreativeTab(9, "brewing");
    public static final LegacyCreativeTab tabMaterials = new LegacyCreativeTab(10, "materials");
    public static final LegacyCreativeTab tabInventory = new LegacyCreativeTab(11, "inventory");

    public static final LegacyCreativeTab field_78030_b = tabBlock;
    public static final LegacyCreativeTab field_78031_c = tabDecorations;
    public static final LegacyCreativeTab field_78028_d = tabRedstone;
    public static final LegacyCreativeTab field_78029_e = tabTransport;
    public static final LegacyCreativeTab field_78026_f = tabMisc;
    public static final LegacyCreativeTab field_78027_g = tabAllSearch;
    public static final LegacyCreativeTab field_78039_h = tabFood;
    public static final LegacyCreativeTab field_78040_i = tabTools;
    public static final LegacyCreativeTab field_78037_j = tabCombat;
    public static final LegacyCreativeTab field_78038_k = tabBrewing;
    public static final LegacyCreativeTab field_78035_l = tabMaterials;
    public static final LegacyCreativeTab field_78036_m = tabInventory;

    private final List<ItemLike> tabItems = new ArrayList<>();
    private final List<Block> tabBlocks = new ArrayList<>();
    private final int tabIndex;
    private CreativeModeTab realTab;

    private String backgroundImageName = "items.png";
    private boolean hasScrollbar = true;
    private boolean drawTitle = true;

    private boolean resolvingIcon = false;
    private ItemStack cachedIconStack = null;

    public LegacyCreativeTab(String label) {
        this(getNextID(), label);
    }

    public LegacyCreativeTab(int index, String label) {
        super(label);
        if (index >= creativeTabArray.length) {
            LegacyCreativeTab[] grown = new LegacyCreativeTab[index + 1];
            System.arraycopy(creativeTabArray, 0, grown, 0, creativeTabArray.length);
            creativeTabArray = grown;
        }
        this.tabIndex = index;
        creativeTabArray[index] = this;
        TABS.add(this);
        this.linkToLegacyTab(this);
    }

    public static int getNextID() {
        return creativeTabArray.length;
    }

    public int getTabIndex() {
        return this.tabIndex;
    }

    public int getTabColumn() {
        if (this.tabIndex > 11) {
            return (this.tabIndex - 12) % 10 % 5;
        }
        return this.tabIndex % 6;
    }

    public boolean isTabInFirstRow() {
        if (this.tabIndex > 11) {
            return (this.tabIndex - 12) % 10 < 5;
        }
        return this.tabIndex < 6;
    }

    public int getTabPage() {
        if (this.tabIndex > 11) {
            return (this.tabIndex - 12) / 10 + 1;
        }
        return 0;
    }

    public boolean hasSearchBar() {
        return this == tabAllSearch || this.tabIndex == 5;
    }

    public int getSearchbarWidth() {
        return 89;
    }

    public void addItem(ItemLike item) {
        if (item == null) return;
        if (!tabItems.contains(item)) {
            tabItems.add(item);
        }
    }

    public void addBlock(Block block) {
        if (block == null) return;
        if (!tabBlocks.contains(block)) {
            tabBlocks.add(block);
        }
    }

    public ItemStack getIconItemStack() {
        if (cachedIconStack != null) return cachedIconStack;
        if (resolvingIcon) return new ItemStack(Items.BARRIER);

        resolvingIcon = true;
        try {
            Item item = callModIconMethod();

            if (item != null && item != Items.AIR) {
                cachedIconStack = new ItemStack(item);
                return cachedIconStack;
            }

            if (!tabItems.isEmpty()) {
                for (ItemLike i : tabItems) {
                    if (i != null && i.asItem() != Items.AIR) {
                        cachedIconStack = new ItemStack(i);
                        return cachedIconStack;
                    }
                }
            }

            if (!tabBlocks.isEmpty()) {
                for (Block b : tabBlocks) {
                    Item blockItem = Item.byBlock(b);
                    if (blockItem != null && blockItem != Items.AIR) {
                        cachedIconStack = new ItemStack(blockItem);
                        return cachedIconStack;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resolvingIcon = false;
        }

        return new ItemStack(Items.BARRIER);
    }

    private Item callModIconMethod() {
        Class<?> myClass = this.getClass();
        if (myClass == LegacyCreativeTab.class) return null;

        try {
            Class<?> current = myClass;
            while (current != null && current != LegacyCreativeTab.class && current != Object.class) {
                for (Method m : current.getDeclaredMethods()) {
                    if (m.getParameterCount() == 0) {
                        String n = m.getName();
                        if (n.equals("getTabIconItem") || n.equals("func_78016_d") ||
                                n.equals("getIconItemStack") || n.equals("func_151244_d")) {

                            m.setAccessible(true);
                            Object res = m.invoke(this);

                            if (res instanceof Item) return (Item) res;
                            if (res instanceof ItemStack) return ((ItemStack) res).getItem();
                            if (res instanceof Block) return ((Block) res).asItem();
                        }
                    }
                }
                current = current.getSuperclass();
            }
        } catch (Exception e) {
            // 辟｡隕・
        }
        return null;
    }

    public static void registerTabs(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            for (LegacyCreativeTab tab : TABS) {
                tab.register(event);
            }
        }
    }

    private void register(RegisterEvent event) {
        final LegacyCreativeTab thisTab = this;

        this.realTab = CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + this.getTabLabel()))
                .icon(this::getIconItemStack)
                .displayItems((params, output) -> {
                    // 繝｡繧ｿ繝・・繧ｿ蟇ｾ蠢懊ヶ繝ｭ繝・け繧貞・逅・
                    for (Block block : tabBlocks) {
                        addBlockWithMetadata(block, thisTab, output);
                    }

                    // 騾壼ｸｸ繧｢繧､繝・Β・医ヶ繝ｭ繝・け繧｢繧､繝・Β莉･螟厄ｼ・
                    for (ItemLike item : tabItems) {
                        if (item == null) continue;

                        if (item instanceof net.minecraft.world.item.BlockItem) {
                            net.minecraft.world.item.BlockItem blockItem = (net.minecraft.world.item.BlockItem) item;
                            Block itemBlock = blockItem.getBlock();
                            if (tabBlocks.contains(itemBlock)) {
                                continue;
                            }
                        }

                        addItemWithMetadata(item, thisTab, output);
                    }
                })
                .build();

        String safeName = this.getTabLabel().replaceAll("[^a-z0-9_]", "_").toLowerCase();
        @SuppressWarnings("removal")
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath("legacy_mod", safeName);
        event.register(Registries.CREATIVE_MODE_TAB, key, () -> realTab);
    }

    /**
     * 繝悶Ο繝・け繧偵Γ繧ｿ繝・・繧ｿ莉倥″縺ｧ霑ｽ蜉
     */
    private void addBlockWithMetadata(Block block, LegacyCreativeTab tab, CreativeModeTab.Output output) {
        Item blockItem = Item.byBlock(block);
        if (blockItem == null || blockItem == Items.AIR) {
            LegacyLoaderMod.LOGGER.warn("LegacyCreativeTab: No item for block " + block);
            return;
        }

        List<ItemStack> subItems = new ArrayList<>();
        boolean foundSubBlocks = false;

        // 繝ｪ繝輔Ξ繧ｯ繧ｷ繝ｧ繝ｳ縺ｧMOD縺ｮgetSubBlocks繧貞他縺ｳ蜃ｺ縺・
        String[] methodNames = {"func_149666_a", "getSubBlocks"};

        for (String methodName : methodNames) {
            if (foundSubBlocks) break;

            try {
                foundSubBlocks = tryInvokeGetSubBlocks(block, methodName, blockItem, tab, subItems);
                // 繧ｵ繝悶い繧､繝・Β縺瑚ｦ九▽縺九▲縺溷ｴ蜷医・蜃ｦ逅・ｒ菫ｮ豁｣
                if (foundSubBlocks && !subItems.isEmpty()) {
                    LegacyLoaderMod.LOGGER.info("LegacyCreativeTab: Collected " + subItems.size() +
                            " sub-items for " + block.getClass().getSimpleName());
                    for (int i = 0; i < subItems.size(); i++) {
                        ItemStack stack = subItems.get(i);
                        if (stack != null && !stack.isEmpty()) {
                            int meta = LegacyItemStackHelper.getMetadata(stack);
                            LegacyLoaderMod.LOGGER.debug("  SubItem[" + i + "]: meta=" + meta +
                                    ", components=" + stack.getComponentsPatch());
                        }
                    }
                } else {
                    // 繝輔か繝ｼ繝ｫ繝舌ャ繧ｯ蜃ｦ逅・..
                }
            } catch (Throwable t) {
                LegacyLoaderMod.LOGGER.debug("LegacyCreativeTab: " + methodName + " failed for " +
                        block.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }

        // ILegacyBlock繧､繝ｳ繧ｿ繝ｼ繝輔ぉ繝ｼ繧ｹ縺九ｉ蜻ｼ縺ｳ蜃ｺ縺暦ｼ医ヵ繧ｩ繝ｼ繝ｫ繝舌ャ繧ｯ・・
        if (!foundSubBlocks && block instanceof ILegacyBlock) {
            try {
                ILegacyBlock legacyBlock = (ILegacyBlock) block;
                int maxMeta = legacyBlock.getMaxMetadata();
                if (maxMeta > 1) {
                    for (int i = 0; i < maxMeta; i++) {
                        ItemStack stack = new ItemStack(blockItem);
                        LegacyItemStackHelper.setMetadata(stack, i);
                        subItems.add(stack);
                    }
                    foundSubBlocks = true;
                    LegacyLoaderMod.LOGGER.info("LegacyCreativeTab: Generated " + maxMeta +
                            " metadata items for " + block.getClass().getSimpleName());
                } else {
                    legacyBlock.getSubBlocks(blockItem, tab, subItems);
                    foundSubBlocks = !subItems.isEmpty();
                }
            } catch (Throwable t) {
                LegacyLoaderMod.LOGGER.debug("LegacyCreativeTab: ILegacyBlock.getSubBlocks failed: " + t.getMessage());
            }
        }

        // 繧ｵ繝悶い繧､繝・Β縺瑚ｦ九▽縺九▲縺溷ｴ蜷・
        if (foundSubBlocks && !subItems.isEmpty()) {
            LegacyLoaderMod.LOGGER.info("LegacyCreativeTab: Adding " + subItems.size() +
                    " sub-items for " + block.getClass().getSimpleName());
            Set<String> emitted = new HashSet<>();
            for (ItemStack stack : subItems) {
                if (stack != null && !stack.isEmpty()) {
                    String key = stack.getItem().toString() + "#" + LegacyItemStackHelper.getMetadata(stack);
                    if (emitted.add(key)) {
                        output.accept(stack);
                    }
                }
            }
        } else {
            // 繝輔か繝ｼ繝ｫ繝舌ャ繧ｯ: 繝｡繧ｿ繝・・繧ｿ謨ｰ縺瑚ｨｭ螳壹＆繧後※縺・ｌ縺ｰ逕滓・
            if (block instanceof LegacyBlock) {
                LegacyBlock lb = (LegacyBlock) block;
                int metaCount = lb.getMaxMetadata();
                if (metaCount > 1) {
                    LegacyLoaderMod.LOGGER.info("LegacyCreativeTab: Fallback - generating " +
                            metaCount + " items for " + block.getClass().getSimpleName());
                    for (int i = 0; i < metaCount; i++) {
                        ItemStack stack = new ItemStack(blockItem);
                        LegacyItemStackHelper.setMetadata(stack, i);
                        output.accept(stack);
                    }
                    return;
                }
            }
            output.accept(blockItem);
        }
    }

    private void addItemWithMetadata(ItemLike itemLike, LegacyCreativeTab tab, CreativeModeTab.Output output) {
        Item item = itemLike != null ? itemLike.asItem() : Items.AIR;
        if (item == null || item == Items.AIR) return;

        List<ItemStack> subItems = new ArrayList<>();
        boolean foundSubItems = false;
        String[] methodNames = {"func_150895_a", "getSubItems"};
        for (String methodName : methodNames) {
            if (foundSubItems) break;
            try {
                foundSubItems = tryInvokeGetSubItems(item, methodName, tab, subItems);
            } catch (Throwable t) {
                LegacyLoaderMod.LOGGER.debug("LegacyCreativeTab: " + methodName + " failed for " +
                        item.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }

        if (foundSubItems && !subItems.isEmpty()) {
            LegacyLoaderMod.LOGGER.info("LegacyCreativeTab: Adding " + subItems.size() +
                    " item sub-items for " + item.getClass().getSimpleName());
            Set<String> emitted = new HashSet<>();
            for (ItemStack stack : subItems) {
                if (stack != null && !stack.isEmpty()) {
                    String key = stack.getItem().toString() + "#" + LegacyItemStackHelper.getMetadata(stack) +
                            "#" + stack.getComponentsPatch();
                    if (emitted.add(key)) {
                        output.accept(stack);
                    }
                }
            }
        } else {
            output.accept(item);
        }
    }

    /**
     * getSubBlocks繝｡繧ｽ繝・ラ繧呈ｧ倥・↑繧ｷ繧ｰ繝阪メ繝｣縺ｧ蜻ｼ縺ｳ蜃ｺ縺励ｒ隧ｦ縺ｿ繧・
     */
    private boolean tryInvokeGetSubBlocks(Block block, String methodName, Item blockItem,
                                          LegacyCreativeTab tab, List<ItemStack> subItems) {
        Class<?> blockClass = block.getClass();
        Class<?> current = blockClass;

        while (current != null && current != Object.class) {
            String className = current.getName();
            if (className.startsWith("com.myname.legacyloader.bridge") ||
                    className.startsWith("net.minecraft.world.level.block.Block")) {
                break;
            }

            Method[] methods;
            try {
                methods = current.getDeclaredMethods();
            } catch (Throwable t) {
                current = current.getSuperclass();
                continue;
            }

            for (Method method : methods) {
                if (!method.getName().equals(methodName)) continue;

                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 3) continue;

                if (!Item.class.isAssignableFrom(paramTypes[0])) continue;
                if (!List.class.isAssignableFrom(paramTypes[2])) continue;

                try {
                    method.setAccessible(true);

                    String param2TypeName = paramTypes[1].getName();

                    if (param2TypeName.equals("net.minecraft.creativetab.CreativeTabs") ||
                            paramTypes[1].isAssignableFrom(CreativeTabs.class)) {
                        method.invoke(block, blockItem, (CreativeTabs) tab, subItems);
                        return true;
                    } else if (param2TypeName.contains("CreativeTab") ||
                            paramTypes[1] == Object.class) {
                        method.invoke(block, blockItem, tab, subItems);
                        return true;
                    }

                } catch (Throwable t) {
                    String msg = t.getMessage();
                    if (msg == null && t.getCause() != null) {
                        msg = t.getCause().getMessage();
                    }
                    LegacyLoaderMod.LOGGER.debug("LegacyCreativeTab: Method " + methodName +
                            " invoke failed on " + current.getSimpleName() + ": " + msg);
                }
            }

            current = current.getSuperclass();
        }

        return false;
    }

    private boolean tryInvokeGetSubItems(Item item, String methodName, LegacyCreativeTab tab,
                                         List<ItemStack> subItems) {
        Class<?> current = item.getClass();

        while (current != null && current != Object.class) {
            String className = current.getName();
            if (className.startsWith("com.myname.legacyloader.bridge") ||
                    className.startsWith("net.minecraft.world.item.Item")) {
                break;
            }

            Method[] methods;
            try {
                methods = current.getDeclaredMethods();
            } catch (Throwable t) {
                current = current.getSuperclass();
                continue;
            }

            for (Method method : methods) {
                if (!method.getName().equals(methodName)) continue;

                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 3) continue;
                if (!Item.class.isAssignableFrom(paramTypes[0])) continue;
                if (!List.class.isAssignableFrom(paramTypes[2])) continue;

                try {
                    method.setAccessible(true);
                    String param2TypeName = paramTypes[1].getName();
                    if (param2TypeName.equals("net.minecraft.creativetab.CreativeTabs") ||
                            paramTypes[1].isAssignableFrom(CreativeTabs.class)) {
                        method.invoke(item, item, (CreativeTabs) tab, subItems);
                        return true;
                    } else if (param2TypeName.contains("CreativeTab") ||
                            paramTypes[1] == Object.class) {
                        method.invoke(item, item, tab, subItems);
                        return true;
                    }
                } catch (Throwable t) {
                    String msg = t.getMessage();
                    if (msg == null && t.getCause() != null) {
                        msg = t.getCause().getMessage();
                    }
                    LegacyLoaderMod.LOGGER.debug("LegacyCreativeTab: Item method " + methodName +
                            " invoke failed on " + current.getSimpleName() + ": " + msg);
                }
            }

            current = current.getSuperclass();
        }

        return false;
    }

    public String getBackgroundImageName() { return backgroundImageName; }
    public boolean drawInForegroundOfTab() { return drawTitle; }
    public boolean shouldHidePlayerInventory() { return hasScrollbar; }

    public LegacyCreativeTab setBackgroundImageName(String texture) {
        this.backgroundImageName = texture == null ? "items.png" : texture;
        return this;
    }

    public LegacyCreativeTab setNoScrollbar() {
        this.hasScrollbar = false;
        return this;
    }

    public LegacyCreativeTab setNoTitle() {
        this.drawTitle = false;
        return this;
    }

    public LegacyCreativeTab func_78025_a(String texture) { return setBackgroundImageName(texture); }
    public LegacyCreativeTab func_78019_g() { return setNoTitle(); }
    public LegacyCreativeTab func_78022_j() { return setNoScrollbar(); }
    public int func_78021_a() { return getTabIndex(); }
    public int func_78020_k() { return getTabColumn(); }
    public boolean func_78023_l() { return isTabInFirstRow(); }
    public int func_151243_f() { return 0; }
    public boolean func_111226_a(Object type) { return false; }
}
