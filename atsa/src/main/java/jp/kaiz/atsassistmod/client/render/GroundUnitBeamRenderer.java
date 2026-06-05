package jp.kaiz.atsassistmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficialItems;
import jp.kaiz.atsassistmod.block.entity.GroundUnitBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

/**
 * Draws a translucent locator beam above a ground unit while the player holds the
 * RTM crowbar (port of TileEntityBeamRenderer). The original used the beacon-beam
 * texture; this uses an additive coloured column for the same locator effect.
 */
public class GroundUnitBeamRenderer implements BlockEntityRenderer<GroundUnitBlockEntity> {
    private static final float R = 0.0F, G = 190F / 255F, B = 246F / 255F, A = 0.5F;
    private static final float HALF = 0.15F;
    private static final float HEIGHT = 64.0F;

    public GroundUnitBeamRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GroundUnitBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean crowbar = player.getMainHandItem().is(RealTrainModUnofficialItems.CROWBAR_ITEM.get())
                || player.getOffhandItem().is(RealTrainModUnofficialItems.CROWBAR_ITEM.get());
        if (!crowbar) {
            return;
        }

        VertexConsumer vc = buffers.getBuffer(RenderType.lightning());
        Matrix4f m = pose.last().pose();
        float lo = 0.5F - HALF;
        float hi = 0.5F + HALF;

        // four vertical faces of a thin column
        face(vc, m, lo, lo, hi, lo);
        face(vc, m, hi, lo, hi, hi);
        face(vc, m, hi, hi, lo, hi);
        face(vc, m, lo, hi, lo, lo);
    }

    private static void face(VertexConsumer vc, Matrix4f m, float x1, float z1, float x2, float z2) {
        vc.addVertex(m, x1, HEIGHT, z1).setColor(R, G, B, A);
        vc.addVertex(m, x1, 0.0F, z1).setColor(R, G, B, A);
        vc.addVertex(m, x2, 0.0F, z2).setColor(R, G, B, A);
        vc.addVertex(m, x2, HEIGHT, z2).setColor(R, G, B, A);
    }
}
