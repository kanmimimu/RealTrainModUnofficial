package com.myname.legacyloader.bridge.block;

import com.myname.legacyloader.bridge.common.LegacyToolType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LegacyBlockProperties {

    private final BlockBehaviour.Properties realProps;

    public LegacyBlockProperties(BlockBehaviour.Properties realProps) {
        this.realProps = realProps;
    }

    public static LegacyBlockProperties create(LegacyMaterial material) {
        return new LegacyBlockProperties(BlockBehaviour.Properties.of());
    }

    public LegacyBlockProperties hardnessAndResistance(float hardness, float resistance) {
        realProps.strength(hardness, resistance);
        return this;
    }

    public LegacyBlockProperties hardnessAndResistance(float hardness) {
        realProps.strength(hardness);
        return this;
    }

    // 笘・ｿｮ豁｣: SoundType 繧貞女縺大・繧後ｋ繧医≧縺ｫ螟画峩 (LegacySoundType縺ｯSoundType繧堤ｶ呎価縺励※縺・ｋ縺溘ａOK)
    public LegacyBlockProperties sound(SoundType soundType) {
        realProps.sound(soundType);
        return this;
    }

    public LegacyBlockProperties noDrops() {
        realProps.noLootTable();
        return this;
    }

    public LegacyBlockProperties harvestLevel(int level) {
        realProps.requiresCorrectToolForDrops();
        return this;
    }

    public LegacyBlockProperties harvestTool(LegacyToolType tool) {
        realProps.requiresCorrectToolForDrops();
        return this;
    }

    public LegacyBlockProperties setRequiresTool() {
        realProps.requiresCorrectToolForDrops();
        return this;
    }

    // --- SRG蜷阪お繧､繝ｪ繧｢繧ｹ ---

    public static LegacyBlockProperties func_200945_a(LegacyMaterial material) {
        return create(material);
    }

    public LegacyBlockProperties func_200948_a(float hardness, float resistance) {
        return hardnessAndResistance(hardness, resistance);
    }

    // SRG蜷阪ｂ SoundType 縺ｧ蜿励￠繧・
    public LegacyBlockProperties func_200947_a(SoundType soundType) {
        return sound(soundType);
    }

    public LegacyBlockProperties func_200943_b(int level) {
        return harvestLevel(level);
    }

    public LegacyBlockProperties func_200950_a() {
        return noDrops();
    }

    public LegacyBlockProperties func_235861_h_() {
        return setRequiresTool();
    }

    public BlockBehaviour.Properties getRealProps() {
        return realProps;
    }
}