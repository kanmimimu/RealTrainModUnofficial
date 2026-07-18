package com.myname.legacyloader.bridge.client.renderer;

import com.myname.legacyloader.LegacyLoaderMod;
import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.client.registry.LegacySimpleBlockRenderingHandler;
import com.myname.legacyloader.bridge.world.LegacySingleBlockAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.ClientHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LegacyISBRHBakedModel implements IDynamicBakedModel {
    private static final ModelProperty<List<BakedQuad>> QUADS = new ModelProperty<>();
    private static final AtomicInteger WARNED_RENDER_FAILURES = new AtomicInteger();

    private final BakedModel fallback;
    private final ResourceLocation blockId;
    private final Block block;
    private final int renderId;
    private final LegacySimpleBlockRenderingHandler handler;
    private final Function<Material, TextureAtlasSprite> textureGetter;
    private volatile List<BakedQuad> inventoryQuads;

    public LegacyISBRHBakedModel(BakedModel fallback, ResourceLocation blockId, Block block, int renderId,
                                 LegacySimpleBlockRenderingHandler handler,
                                 Function<Material, TextureAtlasSprite> textureGetter) {
        this.fallback = fallback;
        this.blockId = blockId;
        this.block = block;
        this.renderId = renderId;
        this.handler = handler;
        this.textureGetter = textureGetter;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        try {
            int meta = state != null && state.hasProperty(LegacyBlock.METADATA) ? state.getValue(LegacyBlock.METADATA) : 0;
            LegacyTessellator.clearCapturedElements();
            LegacyTessellator.beginCaptureAt(pos.getX(), pos.getY(), pos.getZ());

            if (block instanceof LegacyBlock legacyBlock) {
                try {
                    legacyBlock.func_149719_a(level, pos.getX(), pos.getY(), pos.getZ());
                } catch (Throwable ignored) {
                }
            }

            LegacyRenderBlocks renderer = new LegacyRenderBlocks();
            renderer.setCaptureMetadata(meta);
            renderer.setBlockAccess(level, pos.getX(), pos.getY(), pos.getZ());
            handler.renderWorldBlock(level, pos.getX(), pos.getY(), pos.getZ(), block, renderId, renderer);

            List<BakedQuad> baked = bakeCapturedQuads(meta, false);
            if (!baked.isEmpty()) {
                return modelData.derive().with(QUADS, baked).build();
            }
        } catch (Throwable t) {
            warnRenderFailure("world", t);
        } finally {
            LegacyTessellator.clearCapturedElements();
        }
        return fallback.getModelData(level, pos, state, modelData);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData extraData, @Nullable RenderType renderType) {
        if (side != null) return List.of();
        if (state == null) {
            List<BakedQuad> inventory = getInventoryQuads();
            if (!inventory.isEmpty()) return inventory;
        }
        List<BakedQuad> quads = extraData.get(QUADS);
        if (quads != null && !quads.isEmpty()) return quads;
        return fallback.getQuads(state, side, rand, extraData, renderType);
    }

    private List<BakedQuad> getInventoryQuads() {
        List<BakedQuad> cached = inventoryQuads;
        if (cached != null) return cached;
        synchronized (this) {
            if (inventoryQuads == null) {
                inventoryQuads = bakeInventoryQuads(0);
            }
            return inventoryQuads;
        }
    }

    private List<BakedQuad> bakeInventoryQuads(int meta) {
        try {
            LegacyTessellator.clearCapturedElements();
            LegacyTessellator.beginCaptureAt(0, 0, 0);
            LegacyRenderBlocks renderer = new LegacyRenderBlocks();
            renderer.setCaptureMetadata(meta);
            renderer.setBlockAccess(new LegacySingleBlockAccess(block, meta), 0, 0, 0);
            handler.renderInventoryBlock(block, meta, renderId, renderer);
            List<BakedQuad> baked = bakeCapturedQuads(meta, true);
            if (!baked.isEmpty()) return baked;
        } catch (Throwable t) {
            warnRenderFailure("inventory", t);
        } finally {
            LegacyTessellator.clearCapturedElements();
        }
        return List.of();
    }

    private void warnRenderFailure(String phase, Throwable t) {
        int count = WARNED_RENDER_FAILURES.getAndIncrement();
        if (count < 20) {
            LegacyLoaderMod.LOGGER.warn("LegacyLoader: Failed to bake legacy ISBRH {} model for {} renderId {}",
                    phase, blockId, renderId, t);
        }
    }

    private List<BakedQuad> bakeCapturedQuads(int meta, boolean inventory) {
        List<LegacyTessellator.CapturedQuad> captured = LegacyTessellator.consumeCapturedQuads();
        if (captured.isEmpty()) return List.of();
        InventoryFit inventoryFit = inventory ? InventoryFit.from(captured) : null;
        List<BakedQuad> result = new ArrayList<>(captured.size() * 2);
        for (LegacyTessellator.CapturedQuad quad : captured) {
            String iconName = quad.iconName;
            if (iconName == null || iconName.isBlank()) {
                try {
                    var icon = block instanceof LegacyBlock legacyBlock ? legacyBlock.func_149691_a(2, meta) : null;
                    iconName = icon != null ? icon.getIconName() : null;
                } catch (Throwable ignored) {
                }
            }
            ResourceLocation texture = textureLocation(iconName);
            TextureAtlasSprite sprite = textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
            addBakedQuad(result, quad, sprite, inventoryFit, false);
            if (!quad.oneSided) {
                addBakedQuad(result, quad, sprite, inventoryFit, true);
            }
        }
        return result;
    }

    private static void addBakedQuad(List<BakedQuad> result, LegacyTessellator.CapturedQuad quad,
                                     TextureAtlasSprite sprite, @Nullable InventoryFit inventoryFit,
                                     boolean reversed) {
        int[] data = new int[32];
        for (int i = 0; i < 4; i++) {
            int source = reversed ? 3 - i : i;
            int base = i * 8;
            double x = quad.x[source];
            double y = quad.y[source];
            double z = quad.z[source];
            if (inventoryFit != null) {
                x = inventoryFit.fitX(x);
                y = inventoryFit.fitY(y);
                z = inventoryFit.fitZ(z);
            }
            data[base] = Float.floatToRawIntBits((float) x);
            data[base + 1] = Float.floatToRawIntBits((float) y);
            data[base + 2] = Float.floatToRawIntBits((float) z);
            data[base + 3] = quad.color[source];
            data[base + 4] = Float.floatToRawIntBits(sprite.getU(normalizeLegacyUv(quad.u[source])));
            data[base + 5] = Float.floatToRawIntBits(sprite.getV(normalizeLegacyUv(quad.v[source])));
        }
        Direction direction = net.minecraft.client.renderer.block.model.FaceBakery.calculateFacing(data);
        BakedQuad bakedQuad = new BakedQuad(data, -1, direction, sprite, false, false);
        ClientHooks.fillNormal(data, direction);
        result.add(bakedQuad);
    }

    private ResourceLocation textureLocation(String iconName) {
        String namespace = blockId.getNamespace();
        String path = iconName;
        if (path == null || path.isBlank()) {
            path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        }
        path = path.replace('\\', '/').toLowerCase(Locale.ROOT);
        if (path.contains(":")) {
            String[] split = path.split(":", 2);
            namespace = split[0];
            path = split.length > 1 ? split[1] : "";
        }
        if (path.endsWith(".png")) path = path.substring(0, path.length() - 4);
        if (path.startsWith("textures/blocks/")) path = path.substring("textures/blocks/".length());
        else if (path.startsWith("textures/block/")) path = path.substring("textures/block/".length());
        else if (path.startsWith("blocks/")) path = path.substring("blocks/".length());
        else if (path.startsWith("block/")) path = path.substring("block/".length());
        if (!path.startsWith("item/")) path = "block/" + path;
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static float normalizeLegacyUv(double value) {
        double uv = value;
        if (uv > 1.0D || uv < -1.0D) {
            uv /= 16.0D;
        }
        return (float)Math.max(0.0D, Math.min(1.0D, uv));
    }

    private static class InventoryFit {
        private final double centerX;
        private final double centerY;
        private final double centerZ;
        private final double scale;

        private InventoryFit(double centerX, double centerY, double centerZ, double scale) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.scale = scale;
        }

        static InventoryFit from(List<LegacyTessellator.CapturedQuad> captured) {
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;
            for (LegacyTessellator.CapturedQuad quad : captured) {
                for (int i = 0; i < 4; i++) {
                    minX = Math.min(minX, quad.x[i]);
                    minY = Math.min(minY, quad.y[i]);
                    minZ = Math.min(minZ, quad.z[i]);
                    maxX = Math.max(maxX, quad.x[i]);
                    maxY = Math.max(maxY, quad.y[i]);
                    maxZ = Math.max(maxZ, quad.z[i]);
                }
            }
            double sizeX = maxX - minX;
            double sizeY = maxY - minY;
            double sizeZ = maxZ - minZ;
            double maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));
            double scale = maxSize > 1.0E-6D ? 0.9D / maxSize : 1.0D;
            return new InventoryFit((minX + maxX) * 0.5D, (minY + maxY) * 0.5D, (minZ + maxZ) * 0.5D, scale);
        }

        double fitX(double value) {
            return (value - centerX) * scale + 0.5D;
        }

        double fitY(double value) {
            return (value - centerY) * scale + 0.5D;
        }

        double fitZ(double value) {
            return (value - centerZ) * scale + 0.5D;
        }
    }

    @Override public boolean useAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return fallback.isGui3d(); }
    @Override public boolean usesBlockLight() { return fallback.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return fallback.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return fallback.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return fallback.getOverrides(); }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutout());
    }
}
