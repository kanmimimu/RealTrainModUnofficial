package com.portofino.realtrainmodunofficial.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.rail.BlockMarker;
import jp.ngt.rtm.rail.TileEntityMarker;
import jp.ngt.rtm.rail.util.MarkerState;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * 本家 RenderMarkerBlock(Base) の忠実移植 (1.21 BER 版)。
 * - renderDistanceMark: マーカー単体で向き方向に 10m 毎の目盛り (直進+左右45°の3方向、
 *   色付き四角 + "10m".."100m" テキスト)。設置した瞬間に表示される。
 * - renderLine: プレビュー確立後、各 RailMap に沿ったライン + 中央に総延長表示。
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
        BlockState state = marker.getBlockState();
        if (!(state.getBlock() instanceof BlockMarker markerBlock)) {
            return;
        }

        this.renderDistanceMark(marker, markerBlock, state, poseStack, buffer);

        RailMap[] maps = marker.getRailMaps();
        if (maps != null && maps.length > 0) {
            this.renderLine(marker, maps, poseStack, buffer);
        }
    }

    /**
     * 本家 renderDistanceMark: 10m 毎の目盛り (k=-1,0,1 の3方向ファン) + 距離テキスト。
     */
    private void renderDistanceMark(TileEntityMarker marker, BlockMarker block, BlockState state,
                                    PoseStack poseStack, MultiBufferSource buffer) {
        int color = block.markerType == 1 ? 0x0000FF : (block.markerType == 0 ? 0xFF0000 : 0xEC008C);
        float dir = BlockMarker.getMarkerDir(state) * 45.0F;
        int count = RTMConfig.markerDisplayDistance / 10;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0625D, 0.5D);
        poseStack.mulPose(new Quaternionf().rotationY(dir * Mth.DEG_TO_RAD));

        //目盛り四角 (本家: GL_QUADS サイズ0.4)。debugQuads (position_color)
        VertexConsumer quads = buffer.getBuffer(RenderType.debugQuads());
        Matrix4f m = poseStack.last().pose();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float size = 0.4F;
        for (int i = 1; i < count; i++) {
            float moveZ = i * 10.0F;
            for (int k = -1; k <= 1; k++) {
                float moveX = moveZ * k;
                quads.addVertex(m, -size + moveX, 0.01F, size + moveZ).setColor(r, g, b, 1.0F);
                quads.addVertex(m, -size + moveX, 0.01F, -size + moveZ).setColor(r, g, b, 1.0F);
                quads.addVertex(m, size + moveX, 0.01F, -size + moveZ).setColor(r, g, b, 1.0F);
                quads.addVertex(m, size + moveX, 0.01F, size + moveZ).setColor(r, g, b, 1.0F);
            }
        }

        //距離テキスト "10m".."100m"
        Quaternionf cameraRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        for (int j = 0; j < count; j++) {
            float moveZ = (j + 1) * 10.0F;
            String s = (j + 1) * 10 + "m";
            for (int k = -1; k <= 1; k++) {
                float moveX = moveZ * k;
                poseStack.pushPose();
                poseStack.translate(moveX, 0.2F, moveZ);
                //向き回転を打ち消してビルボード化 (本家: -playerViewY - dir)
                poseStack.mulPose(new Quaternionf().rotationY(-dir * Mth.DEG_TO_RAD));
                poseStack.mulPose(cameraRot);
                //本家スケール: glScalef(-0.25, -0.25, 0.25)
                poseStack.scale(-0.25F, -0.25F, 0.25F);
                Matrix4f tm = poseStack.last().pose();
                float x = -this.font.width(s) / 2.0F;
                this.font.drawInBatch(s, x, -10.0F, color | 0xFF000000, false, tm, buffer,
                        Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    /**
     * 本家 renderLine: RailMap に沿ったライン + 中央に総延長 (m)。
     */
    private void renderLine(TileEntityMarker marker, RailMap[] maps, PoseStack poseStack, MultiBufferSource buffer) {
        RailPosition rp = marker.getMarkerRP();
        if (rp == null) {
            return;
        }
        float baseX = (float) (rp.posX - marker.getBlockPos().getX());
        float baseY = (float) (rp.posY - marker.getBlockPos().getY());
        float baseZ = (float) (rp.posZ - marker.getBlockPos().getZ());

        VertexConsumer lines = buffer.getBuffer(RenderType.debugLineStrip(2.0D));
        for (RailMap rm : maps) {
            if (rm == null) continue;
            poseStack.pushPose();
            poseStack.translate(baseX, baseY, baseZ);
            float x0 = (float) (rm.getStartRP().posX - rp.posX);
            float y0 = (float) (rm.getStartRP().posY - rp.posY);
            float z0 = (float) (rm.getStartRP().posZ - rp.posZ);
            poseStack.translate(x0, y0, z0);
            Matrix4f m = poseStack.last().pose();
            int max = (int) ((float) rm.getLength() * 2.0F);
            if (max < 1) max = 1;
            double[] p2 = rm.getRailPos(max, 0);
            double h2 = rm.getRailHeight(max, 0);
            for (int i = 0; i < max + 1; ++i) {
                double[] p1 = rm.getRailPos(max, i);
                lines.addVertex(m, (float) (p1[1] - p2[1]), (float) (rm.getRailHeight(max, i) - h2), (float) (p1[0] - p2[0]))
                        .setColor(0.0F, 0.75F, 0.0F, 1.0F);
            }
            poseStack.popPose();
        }

        //総延長テキスト (本家: 中央、緑 0x00EE00)
        Quaternionf cameraRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        for (RailMap rm : maps) {
            if (rm == null) continue;
            poseStack.pushPose();
            poseStack.translate(baseX, baseY, baseZ);
            int split = (int) (rm.getLength() * 4.0D);
            if (split < 2) split = 2;
            double[] pos = rm.getRailPos(split, split / 2);
            float x0 = (float) (pos[1] - rp.posX);
            float y0 = (float) ((rm.getStartRP().posY + rm.getEndRP().posY) / 2 - rp.posY);
            float z0 = (float) (pos[0] - rp.posZ);
            poseStack.translate(x0, y0 + 0.5F, z0);
            poseStack.mulPose(cameraRot);
            //本家は 0.05 だが視認性向上のため少し大きめ + "m" 付き
            poseStack.scale(-0.1F, -0.1F, 0.1F);
            Matrix4f tm = poseStack.last().pose();
            String s = String.format("%.2fm", rm.getLength());
            float x = -this.font.width(s) / 2.0F;
            this.font.drawInBatch(s, x, -10.0F, 0xFF00EE00, false, tm, buffer,
                    Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return Math.max(128, (int) RTMConfig.markerDisplayDistance + 32);
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityMarker marker) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityMarker marker) {
        return AABB.INFINITE;
    }
}
