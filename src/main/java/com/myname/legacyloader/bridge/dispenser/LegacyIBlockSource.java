package com.myname.legacyloader.bridge.dispenser;

public interface LegacyIBlockSource extends LegacyILocatableSource {
    default Object getWorld() { return null; }
    default int getXInt() { return (int) Math.floor(getX()); }
    default int getYInt() { return (int) Math.floor(getY()); }
    default int getZInt() { return (int) Math.floor(getZ()); }
    default Object getBlockTileEntity() { return null; }
    default double getX() { return getLocation() == null ? 0.0D : getLocation().getX(); }
    default double getY() { return getLocation() == null ? 0.0D : getLocation().getY(); }
    default double getZ() { return getLocation() == null ? 0.0D : getLocation().getZ(); }
}
