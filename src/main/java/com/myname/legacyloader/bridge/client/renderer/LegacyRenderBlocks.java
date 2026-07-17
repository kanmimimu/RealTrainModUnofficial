package com.myname.legacyloader.bridge.client.renderer;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.block.LegacyBlockHelper;
import com.myname.legacyloader.bridge.block.LegacyBlockStairs;
import com.myname.legacyloader.bridge.client.LegacyIcon;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.Field;
import java.util.*;

public class LegacyRenderBlocks {
    public int field_147875_q;
    public int field_147873_r;
    public int field_147871_s;
    public int field_147869_t;
    public int field_147867_u;
    public int field_147865_v;
    public boolean field_147842_e;
    public boolean field_152631_f;

    private double minX;
    private double minY;
    private double minZ;
    private double maxX = 1.0D;
    private double maxY = 1.0D;
    private double maxZ = 1.0D;
    private int metadata;
    private LegacyIcon overrideIcon;
    private boolean explicitBounds;
    private BlockGetter blockAccess;
    private int currentX;
    private int currentY;
    private int currentZ;

    private final Map<String, CapturedElement> elements = new LinkedHashMap<>();

    public void setCaptureMetadata(int metadata) {
        this.metadata = metadata;
    }

    public List<CapturedElement> getCapturedElements() {
        return new ArrayList<>(elements.values());
    }

    public void clearCapturedElements() {
        elements.clear();
    }

    public void func_147782_a(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void setRenderBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        assignRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
        this.explicitBounds = true;
    }

    private void assignRenderBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = clampModel(minX);
        this.minY = clampModel(minY);
        this.minZ = clampModel(minZ);
        this.maxX = clampModel(maxX);
        this.maxY = clampModel(maxY);
        this.maxZ = clampModel(maxZ);
    }

    public void func_147775_a(Block block) {
        loadRenderBoundsFromBlock(block);
        this.explicitBounds = true;
    }

    private void loadRenderBoundsFromBlock(Block block) {
        double[] bounds = readLegacyBounds(block);
        if (bounds != null) {
            assignRenderBounds(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        } else {
            assignRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
    }

    public void setRenderBoundsFromBlock(Block block) {
        func_147775_a(block);
    }

    public boolean func_147784_q(Block block, int x, int y, int z) {
        this.currentX = x;
        this.currentY = y;
        this.currentZ = z;
        if (!explicitBounds) {
            loadRenderBoundsFromBlock(block);
        }
        recordAllFaces(block, metadata);
        return true;
    }

    public boolean renderStandardBlock(Block block, int x, int y, int z) {
        return func_147784_q(block, x, y, z);
    }

    public LegacyIcon func_147787_a(Block block, int side, int meta) {
        return LegacyBlockHelper.func_149691_a(block, side, meta);
    }

    public LegacyIcon getBlockIconFromSideAndMetadata(Block block, int side, int meta) {
        return func_147787_a(block, side, meta);
    }

    public void func_147768_a(Block block, double x, double y, double z, LegacyIcon icon) {
        recordFace(block, 0, icon);
    }

    public void func_147806_b(Block block, double x, double y, double z, LegacyIcon icon) {
        recordFace(block, 1, icon);
    }

    public void func_147761_c(Block block, double x, double y, double z, LegacyIcon icon) {
        recordFace(block, 2, icon);
    }

    public void func_147734_d(Block block, double x, double y, double z, LegacyIcon icon) {
        recordFace(block, 3, icon);
    }

    public void func_147798_e(Block block, double x, double y, double z, LegacyIcon icon) {
        recordFace(block, 4, icon);
    }

    public void func_147764_f(Block block, double x, double y, double z, LegacyIcon icon) {
        recordFace(block, 5, icon);
    }

    public boolean func_147805_b(Block block, int x, int y, int z) {
        return renderStandardBlock(block, x, y, z);
    }

    public boolean func_147722_a(LegacyBlockStairs block, int x, int y, int z) {
        recordAllFaces(block, metadata);
        return true;
    }

    public boolean func_147722_a(Block block, int x, int y, int z) {
        recordAllFaces(block, metadata);
        return true;
    }

    public boolean func_147735_a(Block block, int x, int y, int z) {
        recordAllFaces(block, metadata);
        return true;
    }

    public boolean func_147735_a(LegacyBlock block, int x, int y, int z) {
        recordAllFaces(block, metadata);
        return true;
    }

    public void func_147757_a(LegacyIcon icon) {
        this.overrideIcon = icon;
    }

    public void func_147771_a() {
        this.overrideIcon = null;
    }

    public int func_147801_a(Block block, int x, int y, int z) {
        return 0xFFFFFF;
    }

    public void func_147769_a(BlockGetter world) {
        this.blockAccess = world;
    }

    public void setBlockAccess(BlockGetter world, int x, int y, int z) {
        this.blockAccess = world;
        this.currentX = x;
        this.currentY = y;
        this.currentZ = z;
    }

    private void recordAllFaces(Block block, int meta) {
        for (int side = 0; side < 6; side++) {
            recordFace(block, side, func_147787_a(block, side, meta));
        }
    }

    private void recordFace(Block block, int side, LegacyIcon icon) {
        if (maxX <= minX || maxY <= minY || maxZ <= minZ) return;
        String key = keyForBounds();
        CapturedElement element = elements.computeIfAbsent(key,
                k -> new CapturedElement(minX, minY, minZ, maxX, maxY, maxZ));
        if (overrideIcon != null) icon = overrideIcon;
        if (overrideIcon == null && blockAccess != null && block instanceof LegacyBlock legacyBlock) {
            try {
                LegacyIcon worldIcon = legacyBlock.func_149673_e(blockAccess, currentX, currentY, currentZ, side);
                if (worldIcon != null) icon = worldIcon;
            } catch (Throwable ignored) {
            }
        }
        String iconName = icon != null ? icon.getIconName() : null;
        if (iconName == null || iconName.isBlank()) {
            LegacyIcon fallback = func_147787_a(block, side, metadata);
            iconName = fallback != null ? fallback.getIconName() : null;
        }
        element.faces[Math.max(0, Math.min(5, side))] = cleanIconName(iconName);
        emitQuad(block, side, icon);
    }

    private void emitQuad(Block block, int side, LegacyIcon icon) {
        if (icon == null) return;
        double x0 = currentX + minX, y0 = currentY + minY, z0 = currentZ + minZ;
        double x1 = currentX + maxX, y1 = currentY + maxY, z1 = currentZ + maxZ;
        double[][] xyz = switch (side) {
            case 0 -> new double[][]{{x1,y0,z1},{x1,y0,z0},{x0,y0,z0},{x0,y0,z1}};
            case 1 -> new double[][]{{x1,y1,z1},{x0,y1,z1},{x0,y1,z0},{x1,y1,z0}};
            case 2 -> new double[][]{{x0,y1,z0},{x1,y1,z0},{x1,y0,z0},{x0,y0,z0}};
            case 3 -> new double[][]{{x0,y1,z1},{x0,y0,z1},{x1,y0,z1},{x1,y1,z1}};
            case 4 -> new double[][]{{x0,y1,z1},{x0,y1,z0},{x0,y0,z0},{x0,y0,z1}};
            default -> new double[][]{{x1,y1,z0},{x1,y1,z1},{x1,y0,z1},{x1,y0,z0}};
        };
        double[][] uv = uvForSide(icon, side);
        double[] normal = normalForSide(side);
        LegacyTessellator.captureQuad(icon.getIconName(), xyz, uv, normal[0], normal[1], normal[2], shadeForSide(side));
    }

    private double[][] uvForSide(LegacyIcon icon, int side) {
        double x0 = minX * 16.0D, x1 = maxX * 16.0D;
        double y0 = (1.0D - maxY) * 16.0D, y1 = (1.0D - minY) * 16.0D;
        double z0 = minZ * 16.0D, z1 = maxZ * 16.0D;
        return switch (side) {
            case 0 -> uv(icon, x1, z1, x1, z0, x0, z0, x0, z1);
            case 1 -> uv(icon, x1, z1, x0, z1, x0, z0, x1, z0);
            case 2 -> uv(icon, x0, y0, x1, y0, x1, y1, x0, y1);
            case 3 -> uv(icon, x0, y0, x0, y1, x1, y1, x1, y0);
            case 4 -> uv(icon, z1, y0, z0, y0, z0, y1, z1, y1);
            default -> uv(icon, z0, y0, z1, y0, z1, y1, z0, y1);
        };
    }

    private static double[][] uv(LegacyIcon icon,
                                 double u0, double v0, double u1, double v1,
                                 double u2, double v2, double u3, double v3) {
        return new double[][]{
                {icon.func_94214_a(u0), icon.func_94207_b(v0)},
                {icon.func_94214_a(u1), icon.func_94207_b(v1)},
                {icon.func_94214_a(u2), icon.func_94207_b(v2)},
                {icon.func_94214_a(u3), icon.func_94207_b(v3)}
        };
    }

    private static double[] normalForSide(int side) {
        return switch (side) {
            case 0 -> new double[]{0.0D, -1.0D, 0.0D};
            case 1 -> new double[]{0.0D, 1.0D, 0.0D};
            case 2 -> new double[]{0.0D, 0.0D, -1.0D};
            case 3 -> new double[]{0.0D, 0.0D, 1.0D};
            case 4 -> new double[]{-1.0D, 0.0D, 0.0D};
            default -> new double[]{1.0D, 0.0D, 0.0D};
        };
    }

    private static double shadeForSide(int side) {
        return switch (side) {
            case 0 -> 0.5D;
            case 1 -> 1.0D;
            case 4, 5 -> 0.6D;
            default -> 0.8D;
        };
    }

    private String keyForBounds() {
        return rounded(minX) + "," + rounded(minY) + "," + rounded(minZ) + "," +
                rounded(maxX) + "," + rounded(maxY) + "," + rounded(maxZ);
    }

    private static String rounded(double value) {
        return String.format(Locale.ROOT, "%.5f", value);
    }

    private static String cleanIconName(String name) {
        if (name == null) return null;
        if (name.startsWith("/")) name = name.substring(1);
        String namespace = null;
        if (name.contains(":")) {
            String[] split = name.split(":", 2);
            namespace = split[0].toLowerCase(Locale.ROOT);
            name = split.length > 1 ? split[1] : "";
        }
        if (name.endsWith(".png")) name = name.substring(0, name.length() - 4);
        name = name.toLowerCase(Locale.ROOT).replace("//", "/");
        if (name.startsWith("textures/blocks/")) name = name.substring("textures/blocks/".length());
        else if (name.startsWith("textures/block/")) name = name.substring("textures/block/".length());
        else if (name.startsWith("textures/items/")) name = name.substring("textures/items/".length());
        else if (name.startsWith("textures/item/")) name = name.substring("textures/item/".length());
        else if (name.startsWith("blocks/")) name = name.substring("blocks/".length());
        else if (name.startsWith("items/")) name = name.substring("items/".length());
        return namespace != null ? namespace + ":" + name : name;
    }

    private static double clampModel(double value) {
        return Math.max(-1.0D, Math.min(2.0D, value));
    }

    private static double[] readLegacyBounds(Block block) {
        if (block == null) return null;
        try {
            return new double[]{
                    readDoubleField(block, "minX"),
                    readDoubleField(block, "minY"),
                    readDoubleField(block, "minZ"),
                    readDoubleField(block, "maxX"),
                    readDoubleField(block, "maxY"),
                    readDoubleField(block, "maxZ")
            };
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static double readDoubleField(Object target, String name) throws ReflectiveOperationException {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return ((Number) field.get(target)).doubleValue();
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    public static class CapturedElement {
        public final double minX;
        public final double minY;
        public final double minZ;
        public final double maxX;
        public final double maxY;
        public final double maxZ;
        public final String[] faces = new String[6];

        public CapturedElement(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
    }
}
