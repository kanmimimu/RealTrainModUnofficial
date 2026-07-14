package com.portofino.realtrainmodunofficial.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.portofino.realtrainmodunofficial.vehicle.VehicleDefinition;
import com.portofino.realtrainmodunofficial.vehicle.VehicleRegistry;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Locale;

/**
 * jp.ngt.rtm.entity.train.EntityBogie 用レンダラ — 本家 RenderBogie の忠実移植。
 * 描画位置は車体基準の bogiePos へ補正 (本家: RenderMng の補完値を引いて train 相対位置に置き直す)。
 * モデル供給は暫定で VehicleDefinition/BogieRenderer (Phase 4 で ModelSetTrainClient.bogieModels に置換)。
 */
public class RtmBogieRenderer extends EntityRenderer<EntityBogie> {

    public RtmBogieRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBogie entity) {
        return ResourceLocation.withDefaultNamespace("missingno");
    }

    @Override
    public void render(EntityBogie bogie, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        EntityTrainBase train = bogie.getTrain();
        if (train == null) {
            return;
        }
        VehicleDefinition def = VehicleRegistry.getById(train.getModelName());
        if (def == null || def.getBogies().isEmpty()) {
            return;
        }
        int index = bogie.getBogieId();
        if (index < 0 || index >= def.getBogies().size()) {
            return;
        }
        VehicleDefinition.BogieDefinition bogieDef = def.getBogies().get(index);
        if (bogieDef == null || bogieDef.modelFile() == null || bogieDef.modelFile().isBlank()
                || BogieRenderer.isDummyBogieModel(bogieDef.modelFile())) {
            return;
        }
        //本家組込の ModelBogie.class は Java クラスなので RTMU では読めない。BogieRenderer が
        //標準台車 (ModelBogie_ft1.obj) へ差し替えて描く。
        //
        //以前はここで .class を無条件に return していた。「車体モデル/スクリプトが自前で台車を
        //描く前提」という想定だったが、300 系新幹線のように車体 MQO が body/yukashita/horo の
        //3 グループしか持たず台車をまったく含まない車両では誰も台車を描かず、車体だけが宙に
        //浮いた状態になっていた。
        //
        //自前の走り装置 (車輪グループ) を持つ車両 (蒸気機関車など) だけ、二重描画を避けて
        //スキップする。
        if (bogieDef.modelFile().toLowerCase(Locale.ROOT).endsWith(".class")) {
            com.portofino.realtrainmodunofficial.client.model.MqoModelLoader.MqoModel body =
                com.portofino.realtrainmodunofficial.client.model.MqoModelLoader.loadModelForVehicle(def);
            if (body != null && body.hasOwnWheelGroups()) {
                return;
            }
        }

        //本家 RenderBogie: 補完済み台車位置を引き、車体基準の bogiePos に置き直す
        double bogieFX = Mth.lerp(partialTicks, bogie.xOld, bogie.getX());
        double bogieFY = Mth.lerp(partialTicks, bogie.yOld, bogie.getY());
        double bogieFZ = Mth.lerp(partialTicks, bogie.zOld, bogie.getZ());

        float[][] pos = train.getConfig().getBogiePos();
        Vec3 v31 = new Vec3(pos[index][0], pos[index][1], pos[index][2]);
        v31 = v31.rotateAroundX(Mth.lerp(partialTicks, train.xRotO, train.getXRot()));
        v31 = v31.rotateAroundY(Mth.rotLerp(partialTicks, train.yRotO, train.getYRot()));
        double newX = v31.getX() + Mth.lerp(partialTicks, train.xOld, train.getX());
        double newY = v31.getY() + Mth.lerp(partialTicks, train.yOld, train.getY());
        double newZ = v31.getZ() + Mth.lerp(partialTicks, train.zOld, train.getZ());

        poseStack.pushPose();
        try {
            poseStack.translate(newX - bogieFX, newY - bogieFY, newZ - bogieFZ);

            float roll = Mth.lerp(partialTicks, bogie.prevRotationRoll, bogie.rotationRoll);
            if (Math.abs(roll) > 0.001F) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
            }

            float yaw = Mth.rotLerp(partialTicks, bogie.yRotO, bogie.getYRot());
            float pitch = Mth.lerp(partialTicks, bogie.xRotO, bogie.getXRot());
            //renderWorldBogie が yaw(Y)/-pitch(X)/scale を適用する
            BogieRenderer.renderWorldBogie(poseStack, bogieDef, def, buffer, packedLight, yaw, pitch, partialTicks);
        } finally {
            poseStack.popPose();
        }
    }
}
