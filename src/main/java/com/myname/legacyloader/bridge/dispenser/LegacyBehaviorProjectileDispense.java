package com.myname.legacyloader.bridge.dispenser;

public abstract class LegacyBehaviorProjectileDispense extends LegacyBehaviorDefaultDispenseItem {
    protected abstract Object getProjectileEntity(Object world, LegacyIPosition position);

    protected float func_82498_a() {
        return 6.0F;
    }

    protected float func_82500_b() {
        return 1.1F;
    }
}
