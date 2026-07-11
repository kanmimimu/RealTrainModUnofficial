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
        BlockState state = marker.getBlockState();
        if (!(state.getBlock() instanceof BlockMarker markerBlock)) {
            return;
        }

        //本家: レンチのアンカー移動 (レール形状編集) — キー処理 + マウス追従 + アンカー描画
        if (marker.followMouseMoving && Minecraft.getInstance().player == marker.followingPlayer) {
            this.checkKey(marker);
            this.changeAnchor(marker);
            this.renderAnchor(marker, poseStack, buffer);
        }

        if (!marker.displayDistance || !marker.getState(MarkerState.DISTANCE)) {
            return;
        }

        this.renderDistanceMark(marker, markerBlock, state, poseStack, buffer);

        RailMap[] maps = marker.getRailMaps();
        if (maps != null && maps.length > 0) {
            this.renderLine(marker, maps, poseStack, buffer);
        }
    }

    //---- 本家 RenderMarkerBlock1710: アンカー移動 (レール形状編集) ----

    private static final double FIT_RANGE_SQ = 2.0D * 2.0D;

    private enum MarkerElement {
        NONE(0x000000),
        HORIZONTIAL(0x00FF20),
        VERTICAL(0xFF8800),
        CANT(0xFF00FF);

        final int color;

        MarkerElement(int color) {
            this.color = color;
        }

        int getColor(MarkerElement cur) {
            boolean flag = (cur == this) || cur == MarkerElement.NONE;
            int r = (this.color >> 16 & 0xFF) / (flag ? 1 : 2);
            int g = (this.color >> 8 & 0xFF) / (flag ? 1 : 2);
            int b = (this.color & 0xFF) / (flag ? 1 : 2);
            return (r << 16) | (g << 8) | b;
        }
    }

    /**
     * 本家 checkKey: キー 0-3 で編集対象 (なし/水平/勾配/カント) を切替
     */
    private void checkKey(TileEntityMarker marker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }
        long window = mc.getWindow().getWindow();
        for (int i = 0; i <= 3; i++) {
            if (org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_0 + i) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                marker.editMode = i;
                break;
            }
        }
    }

    /**
     * 本家 changeAnchor: プレイヤーの視線先にアンカーを追従させレール形状を更新
     */
    private void changeAnchor(TileEntityMarker marker) {
        if (marker.getCoreMarker() == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        net.minecraft.world.phys.HitResult target = mc.player.pick(128.0D, 0.0F, true);
        if (!(target instanceof net.minecraft.world.phys.BlockHitResult)) {
            return;
        }

        MarkerElement curElm = MarkerElement.values()[marker.editMode & 3];
        RailPosition rp = marker.getMarkerRP();
        if (rp == null || curElm == MarkerElement.NONE) {
            return;
        }
        net.minecraft.world.phys.Vec3 vec3 = target.getLocation();
        boolean fitOpposite = false;

        RailPosition oppositeRP = this.getOppositeRail(marker);
        if (oppositeRP != null) {
            double dx0 = vec3.x - oppositeRP.posX;
            double dz0 = vec3.z - oppositeRP.posZ;
            if (dx0 * dx0 + dz0 * dz0 <= FIT_RANGE_SQ) {
                vec3 = new net.minecraft.world.phys.Vec3(oppositeRP.posX, oppositeRP.posY, oppositeRP.posZ);
                fitOpposite = true;
            }
        }

        RailPosition neighborRP = this.getNeighborRail(marker);

        double dx = vec3.x - rp.posX;
        double dz = vec3.z - rp.posZ;
        if (dx != 0.0D && dz != 0.0D) {
            float dirRad = (float) Math.atan2(dx, dz);
            float length = (float) (dx / Math.sin(dirRad));
            float yaw = (float) Math.toDegrees(dirRad);

            if (curElm == MarkerElement.HORIZONTIAL) {
                if (neighborRP != null && marker.fitNeighbor) {
                    yaw = Mth.wrapDegrees(neighborRP.anchorYaw + 180.0F);
                }
                rp.anchorYaw = yaw;
                rp.anchorLengthHorizontal = length;
            } else if (curElm == MarkerElement.VERTICAL) {
                float pitch = Mth.wrapDegrees(yaw - rp.anchorYaw);
                if (neighborRP != null && marker.fitNeighbor) {
                    pitch = -neighborRP.anchorPitch;
                } else if (fitOpposite) {
                    double dy = vec3.y - rp.posY;
                    pitch = (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
                }
                rp.anchorPitch = pitch;
                rp.anchorLengthVertical = length;
            } else if (curElm == MarkerElement.CANT) {
                float cant = Mth.wrapDegrees(yaw - rp.anchorYaw);
                if (neighborRP != null && marker.fitNeighbor) {
                    cant = -neighborRP.cantEdge;
                }
                rp.cantEdge = cant;
                RailMap[] maps = marker.getRailMaps();
                if (maps != null && maps.length > 0) {
                    RailMap map = maps[0];
                    float cantAve = (map.getStartRP().cantEdge + map.getEndRP().cantEdge) * 0.5F;
                    map.getStartRP().cantCenter = map.getEndRP().cantCenter = cantAve;
                }
            }

            marker.getCoreMarker().updateRailMap();
        }
    }

    /**
     * 本家 getOppositeRail: プレビューの反対側マーカーの RP
     */
    private RailPosition getOppositeRail(TileEntityMarker marker) {
        if (marker.getRailMaps() == null) {
            return null;
        }
        RailPosition rp = marker.getMarkerRP();
        for (RailMap map : marker.getRailMaps()) {
            if (map.getStartRP().equals(rp)) {
                return map.getEndRP();
            } else if (map.getEndRP().equals(rp)) {
                return map.getStartRP();
            }
        }
        return null;
    }

    /**
     * 本家 getNeighborRail: マーカー隣接位置の既設レールの最寄り RP (接続スナップ用)
     */
    private RailPosition getNeighborRail(TileEntityMarker marker) {
        RailPosition markerRP = marker.getMarkerRP();
        if (markerRP == null || marker.getLevel() == null) {
            return null;
        }
        int[] pos = markerRP.getNeighborPos();
        var tile = marker.getLevel().getBlockEntity(new net.minecraft.core.BlockPos(pos[0], pos[1], pos[2]));
        if (!(tile instanceof jp.ngt.rtm.rail.TileEntityLargeRailBase railBase)) {
            return null;
        }
        jp.ngt.rtm.rail.TileEntityLargeRailCore core = railBase.getRailCore();
        if (core == null || core.getAllRailMaps() == null) {
            return null;
        }
        double distanceSq = Double.MAX_VALUE;
        RailPosition rp = null;
        for (RailMap map : core.getAllRailMaps()) {
            double d2 = sq(markerRP.posX - map.getStartRP().posX) + sq(markerRP.posZ - map.getStartRP().posZ);
            if (d2 < distanceSq) {
                distanceSq = d2;
                rp = map.getStartRP();
            }
            d2 = sq(markerRP.posX - map.getEndRP().posX) + sq(markerRP.posZ - map.getEndRP().posZ);
            if (d2 < distanceSq) {
                distanceSq = d2;
                rp = map.getEndRP();
            }
        }
        return rp;
    }

    private static double sq(double d) {
        return d * d;
    }

    /**
     * 本家 renderAnchor: 水平(緑)/勾配(橙)/カント(桃) のアンカー線 + キー操作ガイド
     */
    private void renderAnchor(TileEntityMarker marker, PoseStack poseStack, MultiBufferSource buffer) {
        RailPosition rp = marker.getMarkerRP();
        if (rp == null) {
            return;
        }
        MarkerElement curElm = MarkerElement.values()[marker.editMode & 3];

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        VertexConsumer lines = buffer.getBuffer(RenderType.debugLineStrip(2.0D));

        //水平アンカー (yaw 方向に anchorLengthHorizontal)
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationY(rp.anchorYaw * Mth.DEG_TO_RAD));
        drawLine(lines, poseStack, 0, 0, 0, 0, 0, rp.anchorLengthHorizontal, MarkerElement.HORIZONTIAL.getColor(curElm));

        //勾配アンカー
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationX(-rp.anchorPitch * Mth.DEG_TO_RAD));
        drawLine(lines, poseStack, 0, 0, 0, 0, 0, rp.anchorLengthVertical, MarkerElement.VERTICAL.getColor(curElm));
        poseStack.popPose();

        //カント
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationZ(rp.cantEdge * Mth.DEG_TO_RAD));
        drawLine(lines, poseStack, 1, 0, 0, -1, 0, 0, MarkerElement.CANT.getColor(curElm));
        poseStack.popPose();
        poseStack.popPose();

        //キー操作ガイド (ビルボードテキスト)
        Quaternionf cameraRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(cameraRot);
        poseStack.scale(0.03F, -0.03F, 0.03F);
        Matrix4f tm = poseStack.last().pose();
        String[] labels = {"key=0 None", "key=1 Horizontial", "key=2 Vertical", "key=3 Cant"};
        MarkerElement[] elms = {MarkerElement.NONE, MarkerElement.HORIZONTIAL, MarkerElement.VERTICAL, MarkerElement.CANT};
        for (int i = 0; i < labels.length; i++) {
            this.font.drawInBatch(labels[i], 0.0F, -10.0F * (i + 1), elms[i].getColor(curElm) | 0xFF000000, false,
                    tm, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void drawLine(VertexConsumer lines, PoseStack poseStack,
                                 float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        Matrix4f m = poseStack.last().pose();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        lines.addVertex(m, x0, y0, z0).setColor(r, g, b, 0.0F);
        lines.addVertex(m, x0, y0, z0).setColor(r, g, b, 1.0F);
        lines.addVertex(m, x1, y1, z1).setColor(r, g, b, 1.0F);
        lines.addVertex(m, x1, y1, z1).setColor(r, g, b, 0.0F);
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
                poseStack.translate(moveX, 2.5F, moveZ);
                //親の向き回転を打ち消してからカメラビルボード (バニラのネームタグ方式)
                poseStack.mulPose(new Quaternionf().rotationY(-dir * Mth.DEG_TO_RAD));
                poseStack.mulPose(cameraRot);
                poseStack.scale(0.25F, -0.25F, 0.25F);
                Matrix4f tm = poseStack.last().pose();
                float x = -this.font.width(s) / 2.0F;
                this.font.drawInBatch(s, x, 0.0F, color | 0xFF000000, false, tm, buffer,
                        Font.DisplayMode.NORMAL, 0, 0xF000F0);
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
            poseStack.translate(x0, y0 + 3.0F, z0);
            poseStack.mulPose(cameraRot);
            //本家は 0.05 だが視認性向上のため少し大きめ + "m" 付き (バニラのネームタグ方式)
            poseStack.scale(0.1F, -0.1F, 0.1F);
            Matrix4f tm = poseStack.last().pose();
            String s = String.format("%.2fm", rm.getLength());
            float x = -this.font.width(s) / 2.0F;
            this.font.drawInBatch(s, x, 0.0F, 0xFF00EE00, false, tm, buffer,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
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
