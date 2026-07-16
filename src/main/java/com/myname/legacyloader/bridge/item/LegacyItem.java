package com.myname.legacyloader.bridge.item;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.client.LegacyIcon;
import com.myname.legacyloader.bridge.client.LegacyIconRegister;
import com.myname.legacyloader.bridge.core.RegistryNameHelper;
import com.myname.legacyloader.bridge.registry.LegacyGameRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LegacyItem extends Item {

    public static final List<LegacyItem> ALL_INSTANCES = new ArrayList<>();
    public static final Map<ResourceLocation, String> TEXTURE_OVERRIDES = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Map<Integer, String>> METADATA_TEXTURES = new ConcurrentHashMap<>();
    public String legacyTextureName;
    public String legacyUnlocalizedName;
    private LegacyCreativeTab assignedTab;
    protected boolean hasSubtypes = false;

    public LegacyItem() {
        this(new Item.Properties());
    }

    public LegacyItem(Item.Properties properties) {
        super(new Item.Properties());
        LegacyGameRegistry.trackItem(this);
        ALL_INSTANCES.add(this);
    }

    // === 繝ｬ繧ｬ繧ｷ繝ｼ繝｡繧ｽ繝・ラ ===

    public Item setUnlocalizedName(String name) {
        this.legacyUnlocalizedName = name;
        if (name != null && !name.isEmpty()) {
            String path = name.replace("item.", "").replace("tile.", "").replace(":", "_").toLowerCase(Locale.ROOT);
            String modid = com.myname.legacyloader.LegacyLoaderMod.CURRENT_LOADING_MODID;
            if (modid == null || "unknown".equals(modid)) modid = "legacy_mod";
            modid = modid.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");

            @SuppressWarnings("removal")
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modid, path);
            RegistryNameHelper.setRegistryName(this, rl);
        }
        return this;
    }

    public Item setTextureName(String name) {
        this.legacyTextureName = name;
        ResourceLocation rl = RegistryNameHelper.getRegistryName(this);
        if (rl != null && name != null && !name.isBlank()) {
            TEXTURE_OVERRIDES.put(rl, cleanTextureName(name));
        }
        return this;
    }

    public Item setCreativeTab(LegacyCreativeTab tab) {
        this.assignedTab = tab;
        if (tab != null) tab.addItem(this);
        return this;
    }

    public Item setMaxStackSize(int size) {
        return this;
    }

    public Item setMaxDamage(int damage) {
        return this;
    }

    public Item setFull3D() {
        return this;
    }

    public Item setNoRepair() {
        return this;
    }

    public Item setContainerItem(Item container) {
        return this;
    }

    public void setHarvestLevel(String toolClass, int level) {
    }

    // 笘・ｿｽ蜉: setHasSubtypes
    public Item setHasSubtypes(boolean hasSubtypes) {
        this.hasSubtypes = hasSubtypes;
        return this;
    }

    public boolean getHasSubtypes() {
        return this.hasSubtypes;
    }

    // === SRG蜷阪お繧､繝ｪ繧｢繧ｹ ===

    public Item func_77655_b(String name) { return setUnlocalizedName(name); }
    public Item func_111206_d(String name) { return setTextureName(name); }
    public Item func_77637_a(LegacyCreativeTab tab) { return setCreativeTab(tab); }
    public Item func_77625_d(int size) { return setMaxStackSize(size); }
    public Item func_77656_e(int damage) { return setMaxDamage(damage); }
    public Item func_77668_e() { return setFull3D(); }
    public Item func_77664_n() { return setFull3D(); }
    public Item func_77612_l() { return setNoRepair(); }
    public Item func_77642_a(Item container) { return setContainerItem(container); }

    // 笘・ｿｽ蜉: func_77627_a = setHasSubtypes
    public Item func_77627_a(boolean hasSubtypes) { return setHasSubtypes(hasSubtypes); }
    public String func_111208_A() { return this.legacyTextureName != null ? this.legacyTextureName : ""; }
    public String func_77658_a() { return this.legacyUnlocalizedName != null ? "item." + this.legacyUnlocalizedName : getDescriptionId(); }
    public String func_77667_c(ItemStack stack) { return func_77658_a(); }
    public int func_77647_b(int metadata) { return metadata; }
    public LegacyIcon func_77617_a(int metadata) { return null; }
    public void func_94581_a(LegacyIconRegister register) {}

    public boolean func_77648_a(ItemStack stack, Player player, Level world, int x, int y, int z,
                                int side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Throwable legacyError = null;
        try {
            BlockPos pos = context.getClickedPos();
            Vec3 hit = context.getClickLocation();
            boolean handled = func_77648_a(context.getItemInHand(), context.getPlayer(), context.getLevel(),
                    pos.getX(), pos.getY(), pos.getZ(), context.getClickedFace().ordinal(),
                    (float) (hit.x - pos.getX()), (float) (hit.y - pos.getY()), (float) (hit.z - pos.getZ()));
            if (handled) return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        } catch (Throwable t) {
            legacyError = t;
        }
        if (tryPlaceBuildCraftPipe(context, legacyError)) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return super.useOn(context);
    }

    private boolean tryPlaceBuildCraftPipe(UseOnContext context, Throwable legacyError) {
        if (!getClass().getName().equals("buildcraft.transport.ItemPipe")) return false;

        try {
            ItemStack stack = context.getItemInHand();
            Level level = context.getLevel();
            Player player = context.getPlayer();
            if (stack == null || stack.isEmpty()) return false;

            Object pipeBlockObj = Class.forName("buildcraft.BuildCraftTransport", false, getClass().getClassLoader())
                    .getField("genericPipeBlock")
                    .get(null);
            if (!(pipeBlockObj instanceof Block pipeBlock)) return false;

            BlockPos pos = context.getClickedPos();
            BlockState clicked = level.getBlockState(pos);
            if (!clicked.canBeReplaced()) {
                pos = pos.relative(context.getClickedFace());
            }

            BlockState existing = level.getBlockState(pos);
            if (!existing.canBeReplaced()) return false;
            BlockState pipeState = pipeBlock.defaultBlockState();
            if (!pipeState.canSurvive(level, pos)) return false;
            if (!level.isUnobstructed(pipeState, pos, net.minecraft.world.phys.shapes.CollisionContext.empty())) return false;

            if (level.isClientSide) {
                return true;
            }

            Object pipe = createBuildCraftPipe(pipeBlock, this);
            if (pipe == null) return false;

            if (!level.setBlock(pos, pipeState, 3)) return false;

            BlockEntity tile = ensureBuildCraftPipeTile(level, pos, pipeState);
            if (tile == null || !tile.getClass().getName().equals("buildcraft.transport.TileGenericPipe")) {
                return false;
            }

            invokeIfPresent(tile, "initialize", new Class<?>[]{pipe.getClass().getSuperclass()}, pipe);
            invokeIfPresent(tile, "initialize", new Class<?>[]{pipe.getClass()}, pipe);
            invokeIfPresent(tile, "sendUpdateToClient", new Class<?>[]{});
            invokeIfPresent(tile, "initializeFromItemMetadata", new Class<?>[]{int.class}, LegacyItemStackHelper.getMetadata(stack));
            invokeIfPresent(pipeBlock, "func_149689_a",
                    new Class<?>[]{Level.class, int.class, int.class, int.class, Object.class, ItemStack.class},
                    level, pos.getX(), pos.getY(), pos.getZ(), player, stack);

            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return true;
        } catch (Throwable fallbackError) {
            if (legacyError != null) {
                com.myname.legacyloader.LegacyLoaderMod.LOGGER.debug("BuildCraft pipe legacy placement failed", legacyError);
            }
            com.myname.legacyloader.LegacyLoaderMod.LOGGER.debug("BuildCraft pipe fallback placement failed", fallbackError);
            return false;
        }
    }

    private Object createBuildCraftPipe(Block pipeBlock, Item item) throws ReflectiveOperationException {
        Class<?> pipeBlockClass = pipeBlock.getClass();
        try {
            return pipeBlockClass.getMethod("createPipe", Item.class).invoke(null, item);
        } catch (NoSuchMethodException e) {
            return Class.forName("buildcraft.transport.BlockGenericPipe", false, getClass().getClassLoader())
                    .getMethod("createPipe", Item.class)
                    .invoke(null, item);
        }
    }

    private BlockEntity ensureBuildCraftPipeTile(Level level, BlockPos pos, BlockState state) throws ReflectiveOperationException {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile != null) return tile;

        Object created = Class.forName("buildcraft.transport.TileGenericPipe", false, getClass().getClassLoader())
                .getDeclaredConstructor()
                .newInstance();
        if (!(created instanceof BlockEntity blockEntity)) return null;
        if (created instanceof com.myname.legacyloader.bridge.tileentity.LegacyTileEntity legacyTile) {
            legacyTile.bindToModernBlock(level, pos, state);
        }
        level.setBlockEntity(blockEntity);
        return blockEntity;
    }

    private Object invokeIfPresent(Object target, String name, Class<?>[] types, Object... args) {
        if (target == null) return null;
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(name, types);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    public void func_150895_a(Item item, LegacyCreativeTab tab, List<ItemStack> list) {
        list.add(new ItemStack(this));
    }

    public void func_150895_a(Item item, Object tab, List<ItemStack> list) {
        LegacyCreativeTab legacyTab = tab instanceof LegacyCreativeTab ? (LegacyCreativeTab) tab : null;
        func_150895_a(item, legacyTab, list);
    }

    // === Block螟画鋤繝ｭ繧ｸ繝・け ===

    public static Item byBlock(Block block) {
        if (block == null) return Items.AIR;
        Item vanillaItem = Item.byBlock(block);
        if (vanillaItem != null && vanillaItem != Items.AIR) return vanillaItem;
        try {
            ResourceLocation regName = RegistryNameHelper.getRegistryName(block);
            if (regName != null) {
                Item forgeItem = BuiltInRegistries.ITEM.get(regName);
                if (forgeItem != null && forgeItem != Items.AIR) {
                    registerBlockToItemMap(block, forgeItem);
                    return forgeItem;
                }
            }
        } catch (Exception e) {
        }
        return Items.AIR;
    }

    public static LegacyItem byBlock(LegacyBlock block) {
        Item item = byBlock((Block) block);
        if (item instanceof LegacyItem) return (LegacyItem) item;
        return null;
    }

    public static void registerBlockToItemMap(Block block, Item item) {
        try {
            Item.BY_BLOCK.put(block, item);
        } catch (Exception e) {
        }
    }

    private static String cleanTextureName(String name) {
        String out = name.trim().replace('\\', '/');
        if (out.startsWith("/")) out = out.substring(1);
        if (out.endsWith(".png")) out = out.substring(0, out.length() - 4);
        if (out.startsWith("textures/")) out = out.substring("textures/".length());
        if (out.startsWith("items/")) out = out.substring("items/".length());
        if (out.startsWith("item/")) out = out.substring("item/".length());
        return out.toLowerCase(Locale.ROOT);
    }

    @Override
    public String getDescriptionId() {
        if (this.legacyUnlocalizedName != null) {
            return "item." + this.legacyUnlocalizedName;
        }
        return super.getDescriptionId();
    }
}
