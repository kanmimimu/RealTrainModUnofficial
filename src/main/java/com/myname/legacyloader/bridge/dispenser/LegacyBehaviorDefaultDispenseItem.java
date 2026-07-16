package com.myname.legacyloader.bridge.dispenser;

public class LegacyBehaviorDefaultDispenseItem implements LegacyIBehaviorDispenseItem {
    public Object dispense(LegacyIBlockSource source, Object stack) {
        return dispenseStack(source, stack);
    }

    protected Object dispenseStack(LegacyIBlockSource source, Object stack) {
        return stack;
    }

    protected void playDispenseSound(LegacyIBlockSource source) {
    }

    protected void spawnDispenseParticles(LegacyIBlockSource source, Object facing) {
    }
}
