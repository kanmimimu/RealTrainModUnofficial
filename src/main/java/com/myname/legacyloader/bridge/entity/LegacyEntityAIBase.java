package com.myname.legacyloader.bridge.entity;

import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Bridge for 1.7.10 EntityAIBase (modern Goal).
 */
public abstract class LegacyEntityAIBase extends Goal {

    // 1.7.10 methods that subclasses override
    public boolean shouldExecute() { return false; }
    public boolean continueExecuting() { return shouldExecute(); }
    public void startExecuting() {}
    public void resetTask() {}
    public void updateTask() {}

    @Override
    public boolean canUse() { return shouldExecute(); }

    @Override
    public boolean canContinueToUse() { return continueExecuting(); }

    @Override
    public void start() { startExecuting(); }

    @Override
    public void stop() { resetTask(); }

    @Override
    public void tick() { updateTask(); }

    // SRG aliases
    public boolean func_75250_a() { return shouldExecute(); }
    public boolean func_75253_b() { return continueExecuting(); }
    public void func_75249_e() { startExecuting(); }
    public void func_75251_c() { resetTask(); }
    public void func_75246_d() { updateTask(); }
}
