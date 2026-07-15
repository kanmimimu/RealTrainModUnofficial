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

        //★毎フレーム、補間済みの車体位置から弦上の bogiePos を求め、実レール(弧)上の最寄り点へ
        //スナップして描く。急カーブでは弦のままだと台車がレールから外れ、逆に per-tick でスナップ
        //すると高速時に台車が車体の毎フレーム補間へ追従しきれず次第に遅れて見える。描画フレーム
        //単位で「補間車体位置→弧」を計算することで、どの速度でも車体と同期しつつレールに乗る。
        double bogieFX = Mth.lerp(partialTicks, bogie.xOld, bogie.getX());
        double bogieFY = Mth.lerp(partialTicks, bogie.yOld, bogie.getY());
        double bogieFZ = Mth.lerp(partialTicks, bogie.zOld, bogie.getZ());

        float[][] cfgPos = train.getConfig().getBogiePos();
        Vec3 v31 = new Vec3(cfgPos[index][0], cfgPos[index][1], cfgPos[index][2]);
        v31 = v31.rotateAroundX(Mth.lerp(partialTicks, train.xRotO, train.getXRot()));
        v31 = v31.rotateAroundY(Mth.rotLerp(partialTicks, train.yRotO, train.getYRot()));
        double chordX = v31.getX() + Mth.lerp(partialTicks, train.xOld, train.getX());
        double chordY = v31.getY() + Mth.lerp(partialTicks, train.yOld, train.getY());
        double chordZ = v31.getZ() + Mth.lerp(partialTicks, train.zOld, train.getZ());
        //弧へスナップ (レール未検出時は弦のまま)。物理状態は変更しない純粋な描画補正。
        double[] arc = bogie.snapToRailArc(chordX, chordY, chordZ);
        double targetX = arc != null ? arc[0] : chordX;
        double targetY = arc != null ? arc[1] : chordY;
        double targetZ = arc != null ? arc[2] : chordZ;

        poseStack.pushPose();
        try {
            poseStack.translate(targetX - bogieFX, targetY - bogieFY, targetZ - bogieFZ);

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
