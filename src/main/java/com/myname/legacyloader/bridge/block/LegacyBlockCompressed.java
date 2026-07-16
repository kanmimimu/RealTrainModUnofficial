package com.myname.legacyloader.bridge.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class LegacyBlockCompressed extends LegacyBlock {
    public LegacyBlockCompressed(LegacyMaterial material) {
        super(material);
    }

    public LegacyBlockCompressed(MapColor color) {
        super(new LegacyMaterial(color));
    }

    // LegacyBlock繧堤ｶ呎価縺励※縺・ｋ縺ｮ縺ｧ縲ヾRG繝｡繧ｽ繝・ラ縺ｯ隕ｪ縺九ｉ邯呎価縺輔ｌ縺ｾ縺・
    // 蠢ｵ縺ｮ縺溘ａ縲√が繝ｼ繝舌・繝ｩ繧､繝峨☆繧句ｴ蜷医↓蛯吶∴縺ｦ譏守､ｺ逧・↓繧ょｮ夂ｾｩ縺励※縺翫￥

    @Override
    public Block func_149663_c(String name) { return setBlockName(name); }
    @Override
    public Block func_149658_d(String name) { return setBlockTextureName(name); }
    @Override
    public Block func_149647_a(com.myname.legacyloader.bridge.item.LegacyCreativeTab tab) { return setCreativeTab(tab); }
    @Override
    public Block func_149711_c(float hardness) { return setHardness(hardness); }
    @Override
    public Block func_149752_b(float resistance) { return setResistance(resistance); }
    @Override
    public Block func_149672_a(LegacySoundType sound) { return setStepSound(sound); }
    @Override
    public Block func_149672_a(Object sound) { return setStepSound(sound); }
    @Override
    public Block func_149713_g(int opacity) { return setLightOpacity(opacity); }
    @Override
    public Block func_149715_a(float value) { return setLightLevel(value); }
    @Override
    public LegacyMaterial func_149688_o() { return getMaterial(); }
    @Override
    public String func_149641_N() { return getTextureName(); }
}