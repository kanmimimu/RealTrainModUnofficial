package net.minecraft.creativetab;

import com.myname.legacyloader.bridge.item.LegacyCreativeTab;

/**
 * 1.7.10縺ｮ net.minecraft.creativetab.CreativeTabs 縺ｮ繧ｹ繧ｿ繝・
 * LegacyCreativeTab縺ｸ縺ｮ繝悶Μ繝・ず縺ｨ縺励※讖溯・
 */
public class CreativeTabs {

    // 1.7.10縺ｮ讓呎ｺ悶ち繝厄ｼ医ム繝溘・・・
    public static final CreativeTabs tabBlock = new CreativeTabs("buildingBlocks");
    public static final CreativeTabs tabDecorations = new CreativeTabs("decorations");
    public static final CreativeTabs tabRedstone = new CreativeTabs("redstone");
    public static final CreativeTabs tabTransport = new CreativeTabs("transportation");
    public static final CreativeTabs tabMisc = new CreativeTabs("misc");
    public static final CreativeTabs tabAllSearch = new CreativeTabs("search");
    public static final CreativeTabs tabFood = new CreativeTabs("food");
    public static final CreativeTabs tabTools = new CreativeTabs("tools");
    public static final CreativeTabs tabCombat = new CreativeTabs("combat");
    public static final CreativeTabs tabBrewing = new CreativeTabs("brewing");
    public static final CreativeTabs tabMaterials = new CreativeTabs("materials");
    public static final CreativeTabs tabInventory = new CreativeTabs("inventory");

    // SRG蜷阪ヵ繧｣繝ｼ繝ｫ繝・
    public static final CreativeTabs field_78030_b = tabBlock;
    public static final CreativeTabs field_78031_c = tabDecorations;
    public static final CreativeTabs field_78028_d = tabRedstone;
    public static final CreativeTabs field_78029_e = tabTransport;
    public static final CreativeTabs field_78026_f = tabMisc;
    public static final CreativeTabs field_78027_g = tabAllSearch;
    public static final CreativeTabs field_78039_h = tabFood;
    public static final CreativeTabs field_78040_i = tabTools;
    public static final CreativeTabs field_78037_j = tabCombat;
    public static final CreativeTabs field_78038_k = tabBrewing;
    public static final CreativeTabs field_78035_l = tabMaterials;
    public static final CreativeTabs field_78036_m = tabInventory;

    private final String label;
    private LegacyCreativeTab linkedTab;

    public CreativeTabs(String label) {
        this.label = label;
    }

    public CreativeTabs(int index, String label) {
        this(label);
    }

    public String getTabLabel() {
        return this.label;
    }

    // SRG: func_78024_c
    public String func_78024_c() {
        return getTabLabel();
    }

    /**
     * LegacyCreativeTab縺ｨ繝ｪ繝ｳ繧ｯ
     */
    public void linkToLegacyTab(LegacyCreativeTab tab) {
        this.linkedTab = tab;
    }

    public LegacyCreativeTab getLinkedTab() {
        return this.linkedTab;
    }
}