package com.myname.legacyloader.bridge.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LegacyGuiScreen extends Screen {

    // 笘・ｿｽ蜉: 1.7.10縺ｮ "mc" 繝輔ぅ繝ｼ繝ｫ繝峨・繧ｨ繧､繝ｪ繧｢繧ｹ
    // Mod縺檎峩謗･繧｢繧ｯ繧ｻ繧ｹ縺ｧ縺阪ｋ繧医≧縺ｫpublic縺ｫ縺励※縺翫″縺ｾ縺・
    public Minecraft mc;
    public Minecraft field_146297_k;

    // 笘・ｿｽ蜉: 蟷・→鬮倥＆縺ｮ繧ｨ繧､繝ｪ繧｢繧ｹ繧ゅｈ縺上お繝ｩ繝ｼ縺ｫ縺ｪ繧九◆繧∬ｿｽ蜉縺励※縺翫″縺ｾ縺・
    // width -> field_146294_l
    // height -> field_146295_m
    public int field_146294_l;
    public int field_146295_m;

    public LegacyGuiScreen() {
        super(Component.literal("Legacy GUI"));

        // 繝輔ぅ繝ｼ繝ｫ繝峨・蛻晄悄蛹・
        this.mc = Minecraft.getInstance();
        this.field_146297_k = this.mc;
    }

    @Override
    public void init() {
        super.init();
        // 逕ｻ髱｢繧ｵ繧､繧ｺ縺悟､峨ｏ縺｣縺溘→縺阪↓繧ｨ繧､繝ｪ繧｢繧ｹ繧よ峩譁ｰ縺吶ｋ
        this.field_146294_l = this.width;
        this.field_146295_m = this.height;
    }

    // 謠冗判蜃ｦ逅・・莠呈鋤諤ｧ (莉雁屓縺ｯ縺ｾ縺螳溯｣・＠縺ｾ縺帙ｓ縺後∝ｰ・擂逧・↓蠢・ｦ√↓縺ｪ繧翫∪縺・
    // 1.7.10: drawScreen(int mouseX, int mouseY, float partialTicks)
    // 1.20.1: render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
}