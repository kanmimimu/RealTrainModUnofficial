package com.portofino.realtrainmodunofficial.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.rail.TileEntityMarker;
import jp.ngt.rtm.rail.util.MarkerState;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

/**
 * 新 TileEntityMarker のレンダラ。
 * 本家 RenderMarkerBlock の距離表示 (メートル表示) を復元する。
 * TODO: グリッド/ベジェ線プレビュー描画 (本家 displayMode 1/2) の移植。
 */
public class MarkerBlockEntityRenderer implements BlockEntityRenderer<TileEntityMarker> {
    private final Font font;

    public MarkerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(TileEntityMarker marker, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!marker.displayDistance || !marker.getState(MarkerState.DISTANCE)) {
            return;
        }
        RailMap[] maps = marker.getRailMaps();
        if (maps == null || maps.length == 0) {
            return;
        }
        //本家準拠: レール長 (m) を表示。複数 RailMap (分岐) は行を分けて表示。
        float yOffset = 0.0F;
        for (RailMap map : maps) {
            if (map == null) {
                continue;
            }
            String text = String.format("%.1fm", map.getLength());
            this.drawBillboardText(text, poseStack, buffer, 1.2F + yOffset);
            yOffset += 0.3F;
        }
    }

    private void drawBillboardText(String text, PoseStack poseStack, MultiBufferSource buffer, float y) {
        poseStack.pushPose();
        poseStack.translate(0.5D, y, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        float x = -this.font.width(text) / 2.0F;
        this.font.drawInBatch(text, x, 0.0F, 0xFFFFFF, false, matrix, buffer,
                Font.DisplayMode.SEE_THROUGH, 0x40000000, 0xF000F0);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return RTMConfig.markerDisplayDistance;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityMarker marker) {
        return true;
    }
}
