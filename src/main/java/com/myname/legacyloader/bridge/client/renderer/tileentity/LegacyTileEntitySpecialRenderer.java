package com.myname.legacyloader.bridge.client.renderer.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class LegacyTileEntitySpecialRenderer implements BlockEntityRenderer<BlockEntity> {

    public LegacyTileEntityRendererDispatcher field_147501_a = LegacyTileEntityRendererDispatcher.field_147556_a;

    // 1.7.10縺ｮMod縺悟ｮ溯｣・☆繧九Γ繧ｽ繝・ラ
    // (TileEntity te, double x, double y, double z, float partialTicks)
    public abstract void renderTileEntityAt(BlockEntity te, double x, double y, double z, float partialTicks);

    // SRG蜷・(func_147500_a) 縺ｮ繧ｨ繧､繝ｪ繧｢繧ｹ
    public void func_147500_a(BlockEntity te, double x, double y, double z, float partialTicks) {
        renderTileEntityAt(te, x, y, z, partialTicks);
    }

    public void setRendererDispatcher(LegacyTileEntityRendererDispatcher dispatcher) {
        this.field_147501_a = dispatcher != null ? dispatcher : LegacyTileEntityRendererDispatcher.field_147556_a;
    }

    public void func_147497_a(LegacyTileEntityRendererDispatcher dispatcher) {
        setRendererDispatcher(dispatcher);
    }

    // 1.20.1縺ｮ謠冗判繝｡繧ｽ繝・ラ (縺薙％縺九ｉ1.7.10縺ｮ繝｡繧ｽ繝・ラ繧貞他縺ｳ蜃ｺ縺・
    @Override
    public void render(BlockEntity te, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 豕ｨ: 1.7.10縺ｮMod縺ｯGL11繧堤峩謗･謫堺ｽ懊☆繧九％縺ｨ繧呈悄蠕・＠縺ｦ縺・∪縺吶・
        // 1.20.1縺ｧ縺ｯPoseStack縺御ｽｿ繧上ｌ縺ｾ縺吶′縲∽ｺ呈鋤諤ｧ繧貞ｮ悟・縺ｫ縺ｨ繧九・縺ｯ髮｣縺励＞縺溘ａ縲・
        // 縺ｨ繧翫≠縺医★蠎ｧ讓・0,0,0)縺ｧ蜻ｼ縺ｳ蜃ｺ縺励※縲√け繝ｩ繝・す繝･縺縺大屓驕ｿ縺励∪縺吶・
        try {
            renderTileEntityAt(te, 0, 0, 0, partialTick);
        } catch (Exception e) {
            // 謠冗判繧ｨ繝ｩ繝ｼ縺ｧ繧ｲ繝ｼ繝縺瑚誠縺｡縺ｪ縺・ｈ縺・↓繧ｭ繝｣繝・メ縺励※縺翫￥
        }
    }

    // field_147501_a (TileEntityRendererDispatcher) 縺ｸ縺ｮ蜿ら・蟇ｾ遲・
    // 蠢・ｦ√↑繧峨ム繝溘・繝輔ぅ繝ｼ繝ｫ繝峨ｒ霑ｽ蜉縺励∪縺吶′縲∽ｸ譌ｦ菫晉蕗
}
