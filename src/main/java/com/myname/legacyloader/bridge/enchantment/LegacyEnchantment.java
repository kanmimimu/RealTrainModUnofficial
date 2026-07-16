package com.myname.legacyloader.bridge.enchantment;

public class LegacyEnchantment {
    public static final LegacyEnchantment[] enchantmentsList = new LegacyEnchantment[256];
    public static LegacyEnchantment[] enchantmentsBookList = new LegacyEnchantment[0];

    public static final LegacyEnchantment protection = new LegacyEnchantment(0, 10, LegacyEnumEnchantmentType.armor);
    public static final LegacyEnchantment fireProtection = new LegacyEnchantment(1, 5, LegacyEnumEnchantmentType.armor);
    public static final LegacyEnchantment featherFalling = new LegacyEnchantment(2, 5, LegacyEnumEnchantmentType.armor_feet);
    public static final LegacyEnchantment blastProtection = new LegacyEnchantment(3, 2, LegacyEnumEnchantmentType.armor);
    public static final LegacyEnchantment projectileProtection = new LegacyEnchantment(4, 5, LegacyEnumEnchantmentType.armor);
    public static final LegacyEnchantment respiration = new LegacyEnchantment(5, 2, LegacyEnumEnchantmentType.armor_head);
    public static final LegacyEnchantment aquaAffinity = new LegacyEnchantment(6, 2, LegacyEnumEnchantmentType.armor_head);
    public static final LegacyEnchantment thorns = new LegacyEnchantment(7, 1, LegacyEnumEnchantmentType.armor);
    public static final LegacyEnchantment sharpness = new LegacyEnchantment(16, 10, LegacyEnumEnchantmentType.weapon);
    public static final LegacyEnchantment smite = new LegacyEnchantment(17, 5, LegacyEnumEnchantmentType.weapon);
    public static final LegacyEnchantment baneOfArthropods = new LegacyEnchantment(18, 5, LegacyEnumEnchantmentType.weapon);
    public static final LegacyEnchantment knockback = new LegacyEnchantment(19, 5, LegacyEnumEnchantmentType.weapon);
    public static final LegacyEnchantment fireAspect = new LegacyEnchantment(20, 2, LegacyEnumEnchantmentType.weapon);
    public static final LegacyEnchantment looting = new LegacyEnchantment(21, 2, LegacyEnumEnchantmentType.weapon);
    public static final LegacyEnchantment efficiency = new LegacyEnchantment(32, 10, LegacyEnumEnchantmentType.digger);
    public static final LegacyEnchantment silkTouch = new LegacyEnchantment(33, 1, LegacyEnumEnchantmentType.digger);
    public static final LegacyEnchantment unbreaking = new LegacyEnchantment(34, 5, LegacyEnumEnchantmentType.breakable);
    public static final LegacyEnchantment fortune = new LegacyEnchantment(35, 2, LegacyEnumEnchantmentType.digger);
    public static final LegacyEnchantment power = new LegacyEnchantment(48, 10, LegacyEnumEnchantmentType.bow);
    public static final LegacyEnchantment punch = new LegacyEnchantment(49, 2, LegacyEnumEnchantmentType.bow);
    public static final LegacyEnchantment flame = new LegacyEnchantment(50, 2, LegacyEnumEnchantmentType.bow);
    public static final LegacyEnchantment infinity = new LegacyEnchantment(51, 1, LegacyEnumEnchantmentType.bow);
    public static final LegacyEnchantment field_151370_z = new LegacyEnchantment(61, 2, LegacyEnumEnchantmentType.fishing_rod);
    public static final LegacyEnchantment field_151369_A = new LegacyEnchantment(62, 2, LegacyEnumEnchantmentType.fishing_rod);

    public final int effectId;
    public final int field_77352_x;
    public LegacyEnumEnchantmentType type;
    protected String name = "";
    private final int weight;

    public LegacyEnchantment() {
        this(-1, 1, LegacyEnumEnchantmentType.all);
    }

    public LegacyEnchantment(int id, int weight) {
        this(id, weight, LegacyEnumEnchantmentType.all);
    }

    public LegacyEnchantment(int id, int weight, int ignoredType) {
        this(id, weight, LegacyEnumEnchantmentType.all);
    }

    public LegacyEnchantment(int id, int weight, LegacyEnumEnchantmentType type) {
        this.effectId = id;
        this.field_77352_x = id;
        this.weight = weight;
        this.type = type == null ? LegacyEnumEnchantmentType.all : type;
        if (id >= 0 && id < enchantmentsList.length) {
            enchantmentsList[id] = this;
            addToBookList(this);
        }
    }

    public int getWeight() {
        return this.weight;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinEnchantability(int level) {
        return 1 + level * 10;
    }

    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 5;
    }

    public int calcModifierDamage(int level, Object source) {
        return 0;
    }

    public float func_152376_a(int level, Object creatureAttribute) {
        return 0.0F;
    }

    public boolean canApplyTogether(LegacyEnchantment other) {
        return this != other;
    }

    public LegacyEnchantment setName(String name) {
        this.name = name == null ? "" : name;
        return this;
    }

    public String getName() {
        return "enchantment." + this.name;
    }

    public String getTranslatedName(int level) {
        return getName() + " " + level;
    }

    public boolean canApply(Object stack) {
        return true;
    }

    public boolean canApplyAtEnchantingTable(Object stack) {
        return canApply(stack);
    }

    public void func_151368_a(Object user, Object target, int level) {
    }

    public void func_151367_b(Object user, Object target, int level) {
    }

    public boolean isAllowedOnBooks() {
        return true;
    }

    public static void addToBookList(LegacyEnchantment enchantment) {
        if (enchantment == null) {
            return;
        }
        LegacyEnchantment[] next = new LegacyEnchantment[enchantmentsBookList.length + 1];
        System.arraycopy(enchantmentsBookList, 0, next, 0, enchantmentsBookList.length);
        next[next.length - 1] = enchantment;
        enchantmentsBookList = next;
    }
}
