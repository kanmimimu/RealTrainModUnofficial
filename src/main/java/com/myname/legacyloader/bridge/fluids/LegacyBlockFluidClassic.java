package com.myname.legacyloader.bridge.fluids;

import com.myname.legacyloader.bridge.block.LegacyBlock;
import com.myname.legacyloader.bridge.block.LegacyMaterial;

public class LegacyBlockFluidClassic extends LegacyBlock {
    protected final LegacyFluid fluid;
    protected final String fluidName;

    public LegacyBlockFluidClassic(LegacyFluid fluid, LegacyMaterial material) {
        super(material);
        this.fluid = fluid;
        this.fluidName = fluid == null ? "" : fluid.getName();
        if (fluid != null) {
            fluid.setBlock(this);
        }
    }

    public LegacyFluid getFluid() {
        return fluid;
    }
}
