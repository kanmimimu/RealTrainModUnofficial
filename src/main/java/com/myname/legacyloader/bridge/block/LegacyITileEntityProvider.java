package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.tileentity.LegacyTileEntity;
import net.minecraft.world.level.Level;

public interface LegacyITileEntityProvider {
    LegacyTileEntity createNewTileEntity(Level world, int meta);
}
