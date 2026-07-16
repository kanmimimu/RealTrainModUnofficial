package com.myname.legacyloader.bridge.client.model;

public class LegacyModelChicken extends LegacyModelBase {
    public LegacyModelRenderer head;
    public LegacyModelRenderer body;
    public LegacyModelRenderer rightLeg;
    public LegacyModelRenderer leftLeg;
    public LegacyModelRenderer rightWing;
    public LegacyModelRenderer leftWing;
    public LegacyModelRenderer bill;
    public LegacyModelRenderer chin;

    public LegacyModelChicken() {
        this.head = new LegacyModelRenderer(this, 0, 0);
        this.bill = new LegacyModelRenderer(this, 14, 0);
        this.chin = new LegacyModelRenderer(this, 14, 4);
        this.body = new LegacyModelRenderer(this, 0, 9);
        this.rightLeg = new LegacyModelRenderer(this, 26, 0);
        this.leftLeg = new LegacyModelRenderer(this, 26, 0);
        this.rightWing = new LegacyModelRenderer(this, 24, 13);
        this.leftWing = new LegacyModelRenderer(this, 24, 13);
    }
}
