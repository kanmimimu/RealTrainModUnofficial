package com.myname.legacyloader.bridge.world;

import java.util.Random;

/** Lightweight 1.7.10 World compatibility shell. Real world access should be added per crash log. */
public class LegacyWorld {
    public boolean field_72995_K = false; // isRemote
    public Random field_73012_v = new Random();
    public int field_73011_w = 0;

    public boolean isRemote() { return field_72995_K; }
    public int getBlockMetadata(int x, int y, int z) { return 0; }
    public Object getBlock(int x, int y, int z) { return null; }
    public boolean setBlock(int x, int y, int z, Object block) { return false; }
    public boolean setBlock(int x, int y, int z, Object block, int meta, int flags) { return false; }
    public boolean setBlockToAir(int x, int y, int z) { return false; }
    public boolean isAirBlock(int x, int y, int z) { return true; }
    public Object getTileEntity(int x, int y, int z) { return null; }
    public void setTileEntity(int x, int y, int z, Object te) {}
    public void removeTileEntity(int x, int y, int z) {}
    public void markBlockForUpdate(int x, int y, int z) {}
    public void notifyBlockChange(int x, int y, int z, Object block) {}
    public void scheduleBlockUpdate(int x, int y, int z, Object block, int ticks) {}
    public boolean spawnEntityInWorld(Object entity) { return false; }
    public Object getClosestPlayer(double x, double y, double z, double d) { return null; }
    public void playSoundEffect(double x, double y, double z, String name, float volume, float pitch) {}
}
