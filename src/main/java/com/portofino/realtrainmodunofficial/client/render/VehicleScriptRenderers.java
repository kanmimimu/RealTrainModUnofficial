package com.portofino.realtrainmodunofficial.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.client.model.MqoModelLoader;
import com.portofino.realtrainmodunofficial.vehicle.VehicleDefinition;
import com.portofino.realtrainmodunofficial.vehicle.VehiclePackLoader;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.renderer.GLRecorder;
import jp.ngt.ngtlib.renderer.model.Face;
import jp.ngt.ngtlib.renderer.model.GroupObject;
import jp.ngt.ngtlib.renderer.model.Material;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.PolygonModel;
import jp.ngt.ngtlib.renderer.model.TextureSet;
import jp.ngt.ngtlib.renderer.model.Vertex;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.render.ModelObject;
import jp.ngt.rtm.render.RenderPass;
import jp.ngt.rtm.render.VehiclePartsRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.script.ScriptEngine;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本家式の車両スクリプト描画 (Phase 3 車両編)。
 * パックの rendererPath スクリプトを Nashorn (本家と同一エンジン) で実行し、
 * renderClass (jp.ngt.rtm.render.VehiclePartsRenderer 等) をリフレクション生成、
 * 毎フレーム render(entity, pass, partialTick) を GLRecorder に記録して PoseStack に再生する。
 *
 * 1.7.10 スクリプトが直接参照する Minecraft クラス (ResourceLocation/TextureUtil 等) は
 * FQN リマップで jp.ngt.mccompat.* に差し替える。//include <path> ディレクティブも解決する。
 */
public final class VehicleScriptRenderers {

    private static final String PRELUDE = com.portofino.realtrainmodunofficial.script.PackScriptSource.PRELUDE;

    private static final Map<String, Scripted> CACHE = new ConcurrentHashMap<>();
    private static final Scripted INVALID = new Scripted(null, null, null);

    private VehicleScriptRenderers() {
    }

    public static Scripted get(VehicleDefinition def) {
        if (def == null || !def.hasScript()) {
            return null;
        }
        Scripted s = CACHE.computeIfAbsent(def.getId(), id -> create(def));
        return s == INVALID ? null : s;
    }

    private static Scripted create(VehicleDefinition def) {
        try {
            String source = VehiclePackLoader.readScriptContent(def);
            if (source == null || source.isBlank()) {
                RealTrainModUnofficial.LOGGER.warn("Vehicle script not readable for {} ({})", def.getId(), def.getScriptPath());
                return INVALID;
            }
            source = com.portofino.realtrainmodunofficial.script.PackScriptSource.prepare(source);

            ScriptEngine se = ScriptUtil.doScript(PRELUDE + source);
            Object rcName = se.get("renderClass");
            if (rcName == null) {
                RealTrainModUnofficial.LOGGER.warn("Vehicle script has no renderClass: {}", def.getId());
                return INVALID;
            }
            Class<?> rc = Class.forName(rcName.toString(), true, ScriptUtil.class.getClassLoader());
            Object instance = newRenderer(rc);
            if (!(instance instanceof VehiclePartsRenderer renderer)) {
                RealTrainModUnofficial.LOGGER.warn("renderClass {} is not a VehiclePartsRenderer ({})", rcName, def.getId());
                return INVALID;
            }
            renderer.setScript(se);
            se.put("renderer", renderer);

            ModelObject modelObject = buildModelObject(def);
            jp.ngt.rtm.modelpack.modelset.ModelSetCompat modelSet =
                    new jp.ngt.rtm.modelpack.modelset.ModelSetCompat(
                            jp.ngt.rtm.modelpack.cfg.TrainConfigAdapter.get(def.getId()));
            //init 内の一部機能 (モニタ等) が失敗しても登録済み Parts で描画を続行する
            renderer.init(modelSet, modelObject);

            RealTrainModUnofficial.LOGGER.info("Vehicle script renderer initialized: {} ({})", def.getId(), rcName);
            return new Scripted(renderer, se, modelObject);
        } catch (Throwable t) {
            RealTrainModUnofficial.LOGGER.warn("Failed to init vehicle script renderer for {}", def.getId(), t);
            return INVALID;
        }
    }

    private static Object newRenderer(Class<?> rc) throws ReflectiveOperationException {
        try {
            return rc.getConstructor(String[].class).newInstance(new Object[]{new String[]{"true"}});
        } catch (NoSuchMethodException e) {
            return rc.getDeclaredConstructor().newInstance();
        }
    }

    private static ModelObject buildModelObject(VehicleDefinition def) {
        List<TextureSet> sets = new ArrayList<>();
        Map<String, String> texMap = def.getTextureOverrides();
        if (texMap != null) {
            for (String path : texMap.values()) {
                //ローダ内部メタ ("|ptmeta=alphablend" 等) を除去 — スクリプトは
                //このパスから "_headLight.png" 等の発光テクスチャ名を合成する
                int meta = path.indexOf("|ptmeta=");
                String clean = meta >= 0 ? path.substring(0, meta) : path;
                sets.add(new TextureSet(new Material(new jp.ngt.mccompat.ResourceLocation("minecraft", clean))));
            }
        }
        if (sets.isEmpty()) {
            sets.add(new TextureSet(new Material(null)));
        }
        ModelObject mo = new ModelObject(sets.toArray(new TextureSet[0]));
        //車体モデルグラフ (CustomAnimator の setFacesFromParts 等が面を参照)
        try {
            String modelFile = def.getModelFile();
            if (modelFile != null && !modelFile.isBlank()) {
                byte[] bytes = NGTFileLoader.findAsset("models/" + modelFile);
                if (bytes == null) {
                    bytes = NGTFileLoader.findAsset(modelFile);
                }
                if (bytes != null) {
                    mo.model = ModelLoader.parse(bytes, modelFile);
                }
            }
        } catch (Exception e) {
            RealTrainModUnofficial.LOGGER.debug("Failed to build model graph for {}: {}", def.getId(), e.toString());
        }
        if (mo.model == null) {
            mo.model = new PolygonModel();
        }
        return mo;
    }

    public static final class Scripted {
        private final VehiclePartsRenderer renderer;
        private final ScriptEngine engine;
        private final ModelObject modelObject;
        private boolean warnedRenderFail;

        //--- スクリプト描画結果のキャッシュ ---------------------------------------------
        //完全に静止した車両 (速度0・ドア/パンタが端点=アニメ中でない) は、スクリプトが
        //partialTick を使っても毎フレーム同じ絵になる。その 1 フレーム分の記録 (GLRecorder)
        //を車両ごとに保持し、状態シグネチャが変わらない限り再生するだけにして、毎フレームの
        //Nashorn 実行を省く。描画結果 (見た目) は一切変えない。動作/アニメ中の車両はキャッシュ
        //しないので滑らかさも不変。
        private final Map<Object, EntityCache> entityCaches =
                java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
        //シグネチャに含めない時間依存アニメ (点滅灯・スクロール幕等) の取りこぼしを救済する
        //安全網。静止車両でもこの間隔で必ず描き直す。10 フレーム = 約 6Hz でリフレッシュ。
        private static final int CACHE_REFRESH_FRAMES = 10;

        //状態が変わった直後 (方向転換・ドア操作等) は、スクリプトが pass==1 で進める時間依存
        //アニメ (座席回転・ドア開閉。本パックは 5000ms) がこの間かけて進む。その間はキャッシュ
        //せず毎フレーム描いて滑らかにアニメさせる。5000ms のアニメに余裕を持たせて 6000ms。
        private static final long ANIMATION_GRACE_MS = 6000L;

        /** 1 車両ぶんのキャッシュ。 */
        private static final class EntityCache {
            boolean valid;
            boolean drew;
            long sig;
            int framesSinceRun;
            //この時刻 (currentTimeMillis) までは毎フレーム描く (アニメ進行中とみなす)。
            long animUntilMs;
            final List<CachedPass> passes = new ArrayList<>();
        }

        /** 1 パスぶんの記録 (通常パス or 発光パス)。 */
        private static final class CachedPass {
            final GLRecorder rec;
            final int pass;
            final Set<String> excluded;
            CachedPass(GLRecorder rec, int pass, Set<String> excluded) {
                this.rec = rec;
                this.pass = pass;
                this.excluded = excluded;
            }
        }

        Scripted(VehiclePartsRenderer renderer, ScriptEngine engine, ModelObject modelObject) {
            this.renderer = renderer;
            this.engine = engine;
            this.modelObject = modelObject;
        }

        /**
         * 毎フレーム: スクリプト render() を記録して再生する。
         *
         * @return true = 描画を担当した
         */
        public boolean render(Object entity, float partialTick, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay,
                              MqoModelLoader.MqoModel bodyModel) {
            //完全静止した車両だけキャッシュ対象にする (アニメ中の車両は毎フレーム描いて滑らかさ維持)。
            boolean canCache = isFullyStatic(entity);
            if (!canCache) {
                this.entityCaches.remove(entity);
                return renderReal(entity, partialTick, poseStack, buffer, packedLight, packedOverlay, bodyModel, null);
            }

            EntityCache ec = this.entityCaches.computeIfAbsent(entity, k -> new EntityCache());
            long sig = signature(entity);
            long now = System.currentTimeMillis();
            //シグネチャ変化 (方向転換・ドア操作・初出現) = スクリプトの時間依存アニメが始まる
            //合図。ここから ANIMATION_GRACE_MS の間は毎フレーム描いてアニメを進める
            //(座席回転・ドア開閉は pass==1 で時計が進むため、毎フレームの Nashorn 実行が必須)。
            if (!ec.valid || ec.sig != sig) {
                ec.animUntilMs = now + ANIMATION_GRACE_MS;
            }
            boolean animating = now < ec.animUntilMs;

            //キャッシュヒット: 記録済みパスを再生するだけ (Nashorn 実行なし)。アニメ中は不可。
            if (!animating && ec.valid && ec.sig == sig && ec.framesSinceRun < CACHE_REFRESH_FRAMES) {
                ec.framesSinceRun++;
                if (!ec.drew) {
                    return false;
                }
                PolygonModel graph = this.modelObject != null ? this.modelObject.model : null;
                for (CachedPass cp : ec.passes) {
                    replay(cp.rec, poseStack, buffer, packedLight, packedOverlay, bodyModel, graph, cp.pass, cp.excluded);
                }
                return true;
            }

            //ミス or アニメ中: 実際に描画しつつ、各パスの記録を集めてキャッシュに保存。
            List<CachedPass> sink = new ArrayList<>();
            boolean drew = renderReal(entity, partialTick, poseStack, buffer, packedLight, packedOverlay, bodyModel, sink);
            ec.valid = true;
            ec.drew = drew;
            ec.sig = sig;
            ec.framesSinceRun = 0;
            ec.passes.clear();
            ec.passes.addAll(sink);
            return drew;
        }

        /**
         * スクリプト描画の実処理 (本家 RenderVehicleBase.doRender と同じ)。
         * sink が非 null のとき、再生した各パスの記録を追加してキャッシュに残す。
         */
        private boolean renderReal(Object entity, float partialTick, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight, int packedOverlay,
                                   MqoModelLoader.MqoModel bodyModel, List<CachedPass> sink) {
            //本家 RenderVehicleBase.doRender: 通常描画 (RenderPass.NORMAL) → 発光描画 (renderBodyLight)
            GLRecorder normal = record(entity, RenderPass.NORMAL.id, partialTick);
            //★ isEmpty ではなく hasGeometry で判定する。スクリプトが何も描かずに落ちると
            //  glPushMatrix だけが残り isEmpty()==false になるため、「描画済み」と誤判定して
            //  素のモデル描画がスキップされ、車体が透明になる (223 系で発生)。
            if (normal == null || !normal.hasGeometry()) {
                return false;
            }
            PolygonModel graph = this.modelObject != null ? this.modelObject.model : null;
            //本家 ResourceState.exclusionParts: スクリプトが描画から外したパーツ (開いたドア等)。
            //スクリプトの render() 内で add/removeExclusionParts が呼ばれた後に読む。
            java.util.Set<String> excluded = exclusionPartsOf(entity);
            replay(normal, poseStack, buffer, packedLight, packedOverlay, bodyModel, graph,
                    RenderPass.NORMAL.id, excluded);
            if (sink != null) {
                //excluded はエンティティ内部のライブ集合なので、キャッシュにはスナップショットを残す。
                sink.add(new CachedPass(normal, RenderPass.NORMAL.id, excluded == null ? null : Set.copyOf(excluded)));
            }
            //★半透明パス (TRANSPARENT=1) をスクリプトに実行させる。本家 RenderVehicleBase は
            //  毎フレーム全パスを回すが、RTMU は従来これを飛ばしていた。座席回転・ドア開閉・
            //  ドアライトのアニメ時計はスクリプト内で「if(pass==1){ setDouble(GetSystemTime()) }」と
            //  pass==1 のときだけ進むため、pass 1 が来ないと時計が永久に止まり、座席が中途半端な
            //  角度・ドアが開いたまま固定される。ここで実行して時計を進める。ジオメトリは通常パス
            //  (pass 0) と重複するので replay しない (記録=時計進行の副作用だけ使い、二重描画を避ける)。
            record(entity, RenderPass.TRANSPARENT.id, partialTick);
            renderBodyLight(entity, partialTick, poseStack, buffer, packedLight, packedOverlay,
                    bodyModel, graph, sink);
            return true;
        }

        private static java.util.Set<String> exclusionPartsOf(Object entity) {
            if (entity instanceof jp.ngt.rtm.entity.vehicle.EntityVehicleBase<?> vehicle) {
                jp.ngt.rtm.modelpack.state.ResourceState state = vehicle.getResourceState();
                if (state != null) {
                    return state.getExclusionParts();
                }
            }
            return null;
        }

        /**
         * 本家 {@code RenderVehicleBase.renderBodyLight} の忠実移植。
         * <pre>
         *   dir         = 進行方向 (0:前 / 1:後)
         *   mode        = ライト状態 (0:消灯 / 1:前照灯 / 2:前照灯+尾灯)
         *   isFrontEmpty = 進行方向側に連結相手がいない (= 先頭車)
         *   isBackEmpty  = 逆側に連結相手がいない       (= 最後尾車)
         *
         *   i=0 (LIGHT       / _light0) : mode == 0 || mode == 1
         *   i=1 (LIGHT_FRONT / _light1) : (mode == 1 && isFrontEmpty)                  || mode == 2
         *   i=2 (LIGHT_BACK  / _light2) : (mode == 1 && !isFrontEmpty && isBackEmpty)  || mode == 2
         * </pre>
         * 先頭車だけが前照灯、最後尾車だけが尾灯、中間車はどちらも点かない。
         */
        private void renderBodyLight(Object entity, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, int packedOverlay,
                                     MqoModelLoader.MqoModel bodyModel, PolygonModel graph, List<CachedPass> sink) {
            if (!(entity instanceof EntityTrainBase train)) {
                return;
            }
            //発光 (Light) マテリアルを 1 つも持たないパックは発光パス自体が無意味。
            //点灯/消灯を別ジオメトリで持つ旧式パックはここに来ない。
            if (bodyModel == null || !bodyModel.hasEmissiveBatches()) {
                return;
            }
            int dir = train.getTrainDirection();
            int mode = train.getTrainStateData(TrainState.TrainStateType.State_Light.id);
            boolean frontEmpty = train.getConnectedTrain(dir) == null;
            boolean backEmpty = train.getConnectedTrain(1 - dir) == null;

            for (int i = 0; i < 3; i++) {
                boolean doRender = switch (i) {
                    case 0 -> mode == 0 || mode == 1;
                    case 1 -> (mode == 1 && frontEmpty) || mode == 2;
                    default -> (mode == 1 && !frontEmpty && backEmpty) || mode == 2;
                };
                if (!doRender) {
                    continue;
                }
                int pass = RenderPass.LIGHT.id + i;
                GLRecorder rec = record(entity, pass, partialTick);
                //スクリプトが発光パスの途中で落ちても、そこまでに描いたライトは活かす。
                if (rec == null || !rec.hasGeometry()) {
                    continue;
                }
                replay(rec, poseStack, buffer, packedLight, packedOverlay, bodyModel, graph, pass);
                if (sink != null) {
                    sink.add(new CachedPass(rec, pass, null));
                }
            }
        }

        /**
         * 完全に静止した車両か? = 速度0 かつ ドア/パンタが端点 (アニメ中でない)。
         * ここが true の間だけ描画結果をキャッシュする。動作/アニメ中は毎フレーム描いて
         * partialTick 補間の滑らかさを保つ。
         */
        private static boolean isFullyStatic(Object entity) {
            if (!(entity instanceof EntityTrainBase train)) {
                return false;
            }
            if (train.getSpeed() != 0.0F) {
                return false;
            }
            if (entity instanceof jp.ngt.rtm.entity.vehicle.EntityVehicleBase<?> v) {
                if (!atEndpoint(v.doorMoveL, jp.ngt.rtm.entity.vehicle.EntityVehicleBase.MAX_DOOR_MOVE)
                        || !atEndpoint(v.doorMoveR, jp.ngt.rtm.entity.vehicle.EntityVehicleBase.MAX_DOOR_MOVE)
                        || !atEndpoint(v.pantograph_F, jp.ngt.rtm.entity.vehicle.EntityVehicleBase.MAX_PANTOGRAPH_MOVE)
                        || !atEndpoint(v.pantograph_B, jp.ngt.rtm.entity.vehicle.EntityVehicleBase.MAX_PANTOGRAPH_MOVE)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean atEndpoint(int value, int max) {
            return value <= 0 || value >= max;
        }

        /**
         * 描画結果を左右する車両状態のシグネチャ。これが変わったら即座に描き直す。
         * (時間依存アニメなど、ここに含まれない変化は CACHE_REFRESH_FRAMES で救済する)
         */
        private long signature(Object entity) {
            EntityTrainBase train = (EntityTrainBase) entity;
            long h = 1125899906842597L;
            h = 31L * h + Float.floatToIntBits(train.wheelRotationR);
            h = 31L * h + Float.floatToIntBits(train.wheelRotationL);
            int dir = train.getTrainDirection();
            h = 31L * h + dir;
            h = 31L * h + train.getTrainStateData(TrainState.TrainStateType.State_Light.id);
            h = 31L * h + train.getTrainStateData(TrainState.TrainStateType.State_Destination.id);
            h = 31L * h + (train.getConnectedTrain(dir) == null ? 0 : 1);
            h = 31L * h + (train.getConnectedTrain(1 - dir) == null ? 0 : 1);
            if (entity instanceof jp.ngt.rtm.entity.vehicle.EntityVehicleBase<?> v) {
                h = 31L * h + v.doorMoveL;
                h = 31L * h + v.doorMoveR;
                h = 31L * h + v.pantograph_F;
                h = 31L * h + v.pantograph_B;
            }
            Set<String> ex = exclusionPartsOf(entity);
            h = 31L * h + (ex == null ? 0 : ex.hashCode());
            return h;
        }

        /** スクリプトの render(entity, pass, partialTick) を 1 パスぶん記録する。 */
        private GLRecorder record(Object entity, int pass, float partialTick) {
            GLRecorder rec = new GLRecorder();
            GLRecorder.activate(rec);
            try {
                this.renderer.currentMatId = 0;
                this.renderer.currentPass = pass;
                this.renderer.render(entity, pass, partialTick);
            } catch (Throwable t) {
                if (!this.warnedRenderFail) {
                    this.warnedRenderFail = true;
                    RealTrainModUnofficial.LOGGER.warn("Vehicle script render failed", t);
                }
                return null;
            } finally {
                this.renderer.currentPass = 0;
                GLRecorder.deactivate();
            }
            //スクリプトが落ちても記録は捨てない。途中まで描いていればそれは活かす
            //(発光パスの途中で落ちる車両が多く、記録ごと捨てるとライトが消える)。
            //「何も描かずに落ちた」かどうかは呼び出し側が rec.hasGeometry() で判断する。
            this.renderer.consumeScriptFailure();
            return rec;
        }
    }

    static void replay(GLRecorder rec, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay, MqoModelLoader.MqoModel model,
                       PolygonModel bodyGraph) {
        replay(rec, poseStack, buffer, packedLight, packedOverlay, model, bodyGraph, RenderPass.NORMAL.id, null);
    }

    static void replay(GLRecorder rec, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay, MqoModelLoader.MqoModel model,
                       PolygonModel bodyGraph, int legacyPass) {
        replay(rec, poseStack, buffer, packedLight, packedOverlay, model, bodyGraph, legacyPass, null);
    }

    /**
     * @param legacyPass 本家 RenderPass の id。2 以上 (LIGHT/LIGHT_FRONT/LIGHT_BACK) のとき、
     *                   スクリプトが描いたグループを ***_light0/1/2.png に差し替えて重ねる
     *                   (本家 ModelObject.renderWithTexture と同じ)。
     * @param excluded   本家 ResourceState.exclusionParts。スクリプトが「今は描かない」と指定した
     *                   パーツ (ドアが開いた側の扉など)。null なら除外なし。
     */
    @SuppressWarnings("unchecked")
    static void replay(GLRecorder rec, PoseStack poseStack, MultiBufferSource buffer,
                               int packedLight, int packedOverlay, MqoModelLoader.MqoModel model,
                               PolygonModel bodyGraph, int legacyPass, java.util.Set<String> excluded) {
        int light = packedLight;
        ResourceLocation overrideTex = null;
        //スクリプトの glColor4f (発光オーバーレイの強度等に使用)
        float colR = 1.0F, colG = 1.0F, colB = 1.0F, colA = 1.0F;
        int depth = 0;
        for (GLRecorder.Cmd cmd : rec.getCommands()) {
            switch (cmd.op) {
                case PUSH -> {
                    poseStack.pushPose();
                    depth++;
                }
                case POP -> {
                    if (depth > 0) {
                        poseStack.popPose();
                        depth--;
                    }
                }
                case TRANSLATE -> poseStack.translate(cmd.a, cmd.b, cmd.c);
                case ROTATE -> {
                    if (cmd.payload instanceof org.joml.Quaternionf quat) {
                        poseStack.mulPose(quat);
                    }
                }
                case SCALE -> poseStack.scale(cmd.a, cmd.b, cmd.c);
                //負値 = 元の環境光へ戻す (スクリプトの車内発光ブロック終端)
                case BRIGHTNESS -> light = cmd.a < 0 ? packedLight : (int) cmd.a;
                case COLOR -> {
                    colR = cmd.a;
                    colG = cmd.b;
                    colB = cmd.c;
                    colA = cmd.d;
                }
                case BIND_TEXTURE -> overrideTex = cmd.payload instanceof ResourceLocation rl ? rl : null;
                case RENDER_PARTS, RENDER_GROUPS -> {
                    if (cmd.payload instanceof Set<?> names) {
                        if (overrideTex != null && bodyGraph != null) {
                            //テクスチャ差し替え中 (発光/ヘッドライト等): モデルグラフから
                            //同グループの面を差し替えテクスチャで描画 (UV は MQO のまま)
                            for (Object name : names) {
                                drawModelGroup(bodyGraph, String.valueOf(name), poseStack, buffer,
                                        light, packedOverlay, overrideTex, colR, colG, colB, colA);
                            }
                        } else if (model != null && legacyPass >= RenderPass.LIGHT.id) {
                            //発光パス: Light マテリアルの面だけを ***_light0/1/2.png で描き直す
                            //(本家 ModelObject.renderWithTexture と同じ。グループ名は見ない)
                            model.renderNamedGroupsEmissive(poseStack, buffer, light, packedOverlay,
                                    (Set<String>) names, legacyPass);
                        } else if (model != null) {
                            //translucent=false は全バッチ描画 (renderSelectedBatches のフィルタ仕様)。
                            //excluded はスクリプトが除外指定したパーツ (開いたドア等) を落とす。
                            model.renderNamedGroups(poseStack, buffer, light, packedOverlay, false,
                                    (Set<String>) names, null, excluded);
                        }
                    }
                }
                case DRAW_TESS -> {
                    if (cmd.payload instanceof GLRecorder.TessDraw draw) {
                        drawTess(draw, poseStack, buffer, light, packedOverlay, overrideTex);
                    }
                }
                case DRAW_MODEL_GROUP -> {
                    if (cmd.payload instanceof PolygonModel pm) {
                        drawModelGroup(pm, cmd.name, poseStack, buffer, light, packedOverlay, overrideTex,
                                colR, colG, colB, colA);
                    }
                }
            }
        }
        while (depth > 0) {
            poseStack.popPose();
            depth--;
        }
    }

    private static final int GL_TRIANGLES = 4;
    private static final int GL_TRIANGLE_FAN = 6;
    private static final int GL_QUADS = 7;

    private static void drawTess(GLRecorder.TessDraw draw, PoseStack poseStack, MultiBufferSource buffer,
                                 int light, int overlay, ResourceLocation texture) {
        ResourceLocation tex = texture != null ? texture
                : ResourceLocation.withDefaultNamespace("textures/misc/white.png");
        //本家はブレンド有効の即時描画 — 半透明テクスチャ (方向幕/LCD) を正しく合成する
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(tex));
        int stride = 9;
        int count = draw.verts.length / stride;
        switch (draw.mode) {
            case GL_QUADS -> {
                for (int q = 0; q + 4 <= count; q += 4) {
                    emitQuad(vc, poseStack, draw.verts, q, q + 1, q + 2, q + 3, light, overlay);
                }
            }
            case GL_TRIANGLES -> {
                for (int t = 0; t + 3 <= count; t += 3) {
                    emitQuad(vc, poseStack, draw.verts, t, t + 1, t + 2, t + 2, light, overlay);
                }
            }
            case GL_TRIANGLE_FAN -> {
                for (int t = 1; t + 2 <= count; t++) {
                    emitQuad(vc, poseStack, draw.verts, 0, t, t + 1, t + 1, light, overlay);
                }
            }
            default -> {
                //LINES 等は未対応
            }
        }
    }

    private static void emitQuad(VertexConsumer vc, PoseStack poseStack, float[] v,
                                 int i0, int i1, int i2, int i3, int light, int overlay) {
        Vector3f normal = quadNormal(v, i0, i1, i2);
        PoseStack.Pose pose = poseStack.last();
        emitVertex(vc, pose, v, i0, normal, light, overlay);
        emitVertex(vc, pose, v, i1, normal, light, overlay);
        emitVertex(vc, pose, v, i2, normal, light, overlay);
        emitVertex(vc, pose, v, i3, normal, light, overlay);
    }

    private static Vector3f quadNormal(float[] v, int i0, int i1, int i2) {
        int a = i0 * 9, b = i1 * 9, c = i2 * 9;
        float ax = v[b] - v[a], ay = v[b + 1] - v[a + 1], az = v[b + 2] - v[a + 2];
        float bx = v[c] - v[a], by = v[c + 1] - v[a + 1], bz = v[c + 2] - v[a + 2];
        Vector3f n = new Vector3f(ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx);
        if (n.lengthSquared() < 1.0e-8F) {
            n.set(0.0F, 1.0F, 0.0F);
        } else {
            n.normalize();
        }
        return n;
    }

    private static void emitVertex(VertexConsumer vc, PoseStack.Pose pose, float[] v, int index,
                                   Vector3f normal, int light, int overlay) {
        int o = index * 9;
        Matrix4f mat = pose.pose();
        //バニラの addVertex(Matrix4f,..)/setNormal(Pose,..) は頂点ごとに new Vector3f() を
        //確保する。スクリプト車両は 1 フレームに数万頂点を流すため GC 負荷が大きい。
        //変換式は同一のまま確保だけを避ける (見た目は不変)。
        VertexConsumer written = VertexWriter.addVertex(vc, mat, v[o], v[o + 1], v[o + 2])
                .setColor(v[o + 5], v[o + 6], v[o + 7], v[o + 8])
                .setUv(v[o + 3], v[o + 4])
                .setOverlay(overlay)
                .setLight(light);
        VertexWriter.setNormal(written, pose, normal.x, normal.y, normal.z);
    }

    private static void drawModelGroup(PolygonModel pm, String groupName, PoseStack poseStack,
                                       MultiBufferSource buffer, int light, int overlay, ResourceLocation texture,
                                       float colR, float colG, float colB, float colA) {
        GroupObject group = null;
        for (GroupObject g : pm.groupObjects) {
            if (g.name.equalsIgnoreCase(groupName)) {
                group = g;
                break;
            }
        }
        if (group == null || group.faces.isEmpty()) {
            return;
        }
        ResourceLocation tex = texture != null ? texture
                : ResourceLocation.withDefaultNamespace("textures/misc/white.png");
        //発光テクスチャ (***_light*.png) は黒地=非発光なので加算合成+フルブライトで重ねる
        //(通常ブレンドだと黒地が不透明に描かれて方向幕等が黒く潰れる)
        boolean lightOverlay = NGTFileLoader.isLightOverlayTexture(tex);
        if (lightOverlay) {
            light = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
        }
        //本家はブレンド有効の即時描画 (モニタ/発光パーツ)
        VertexConsumer vc = buffer.getBuffer(lightOverlay ? RenderType.eyes(tex) : RenderType.entityTranslucent(tex));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        for (Face face : group.faces) {
            int n = face.vertices.length;
            if (n < 3) {
                continue;
            }
            //四角形はそのまま、三角形は縮退クアッド、5角以上は扇状分割
            for (int t = 0; t < (n == 4 ? 1 : n - 2); t++) {
                int[] idx = n == 4 ? new int[]{0, 1, 2, 3} : new int[]{0, t + 1, t + 2, t + 2};
                Vector3f normal = faceNormal(face, idx);
                for (int k = 0; k < 4; k++) {
                    Vertex vert = face.vertices[idx[k]];
                    float u = 0.0F, vv = 0.0F;
                    if (face.uvs != null && idx[k] * 2 + 1 < face.uvs.length) {
                        u = face.uvs[idx[k] * 2];
                        vv = face.uvs[idx[k] * 2 + 1];
                    }
                    VertexConsumer written = VertexWriter.addVertex(vc, mat, vert.x, vert.y, vert.z)
                            .setColor(colR, colG, colB, colA)
                            .setUv(u, vv)
                            .setOverlay(overlay)
                            .setLight(light);
                    VertexWriter.setNormal(written, pose, normal.x, normal.y, normal.z);
                }
            }
        }
    }

    private static Vector3f faceNormal(Face face, int[] idx) {
        Vertex v0 = face.vertices[idx[0]];
        Vertex v1 = face.vertices[idx[1]];
        Vertex v2 = face.vertices[idx[2]];
        float ax = v1.x - v0.x, ay = v1.y - v0.y, az = v1.z - v0.z;
        float bx = v2.x - v0.x, by = v2.y - v0.y, bz = v2.z - v0.z;
        Vector3f n = new Vector3f(ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx);
        if (n.lengthSquared() < 1.0e-8F) {
            n.set(0.0F, 1.0F, 0.0F);
        } else {
            n.normalize();
        }
        return n;
    }
}
