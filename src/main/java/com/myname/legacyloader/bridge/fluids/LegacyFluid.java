package com.myname.legacyloader.bridge.fluids;

import com.myname.legacyloader.bridge.client.LegacyIcon;
import net.minecraft.world.level.block.Block;

public class LegacyFluid {
    private static int nextId = 1;

    private final String name;
    private final int id;
    private Block block;
    private int luminosity;

    public LegacyFluid(String name) {
        this.name = name == null ? "" : name;
        this.id = nextId++;
    }

    public String getName() { return name; }
    public int getID() { return id; }
    public LegacyFluid setDensity(int density) { return this; }
    public LegacyFluid setViscosity(int viscosity) { return this; }
    public LegacyFluid setLuminosity(int luminosity) { this.luminosity = luminosity; return this; }
    public int getLuminosity(LegacyFluidStack stack) { return luminosity; }
    public LegacyFluid setBlock(Block block) { this.block = block; return this; }
    public Block getBlock() { return block; }
    public int getColor(LegacyFluidStack stack) { return 0xFFFFFFFF; }
    public String getLocalizedName(LegacyFluidStack stack) { return name; }
    public LegacyIcon getIcon(LegacyFluidStack stack) { return null; }
    public LegacyFluid setIcons(LegacyIcon still, LegacyIcon flowing) { return this; }
}
