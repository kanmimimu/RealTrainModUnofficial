package com.myname.legacyloader.bridge.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Bridge for 1.7.10 MovingObjectPosition (modern HitResult).
 */
public class LegacyMovingObjectPosition {

    public enum MovingObjectType {
        MISS, BLOCK, ENTITY
    }

    public MovingObjectType typeOfHit;

    // Block hit data
    public int blockX, blockY, blockZ;
    public int sideHit;

    // Entity hit
    public Object entityHit;

    // Hit vector
    public Vec3 hitVec;

    public LegacyMovingObjectPosition(HitResult result) {
        if (result instanceof BlockHitResult bhr) {
            typeOfHit = MovingObjectType.BLOCK;
            BlockPos pos = bhr.getBlockPos();
            blockX = pos.getX();
            blockY = pos.getY();
            blockZ = pos.getZ();
            sideHit = bhr.getDirection().get3DDataValue();
            hitVec = bhr.getLocation();
        } else if (result instanceof EntityHitResult ehr) {
            typeOfHit = MovingObjectType.ENTITY;
            entityHit = ehr.getEntity();
            hitVec = ehr.getLocation();
        } else {
            typeOfHit = MovingObjectType.MISS;
        }
    }

    public LegacyMovingObjectPosition(int x, int y, int z, int side, Vec3 hitVec) {
        this.typeOfHit = MovingObjectType.BLOCK;
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
        this.sideHit = side;
        this.hitVec = hitVec;
    }

    public boolean isBlock() { return typeOfHit == MovingObjectType.BLOCK; }
    public boolean isEntity() { return typeOfHit == MovingObjectType.ENTITY; }
}
