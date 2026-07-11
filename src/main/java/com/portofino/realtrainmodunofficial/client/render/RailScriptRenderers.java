package com.portofino.realtrainmodunofficial.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.client.model.MqoModelLoader;
import com.portofino.realtrainmodunofficial.rail.RailDefinition;
import com.portofino.realtrainmodunofficial.rail.RailPackLoader;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.renderer.GLRecorder;
import jp.ngt.ngtlib.renderer.model.Material;
import jp.ngt.ngtlib.renderer.model.TextureSet;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore;
import jp.ngt.rtm.rail.util.RailMap;
import jp.ngt.rtm.render.ModelObject;
import jp.ngt.rtm.render.RailPartsRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * レール定義ごとの本家式スクリプトレンダラ (jp.ngt.rtm.render.RailPartsRenderer) の管理。
 * Phase 3 先行実装:
 * - 本物の Nashorn + 実 jp.ngt クラスでレールスクリプトを実行
 * - renderRailStatic (スクリプト自前描画) を GLRecorder に記録
 * - 本家デフォルト配置 (0.5m 毎にモデルを yaw/pitch/roll 回転して設置) を
 *   shouldRenderObject(tile, objName, len, pos) でオブジェクト毎にフィルタして記録
 * - BE ごとの記録キャッシュを PoseStack へ再生 (本家 DisplayList 相当)
 */
public final class RailScriptRenderers {

    /**
     * スクリプトが GL11 として使うファサードのプリバインド (mozilla_compat の importPackage より優先される)。
     */
    private static final String PRELUDE =
            "var GL11 = Java.type('jp.ngt.ngtlib.renderer.GL11Facade');\n" +
            "var GL12 = GL11;\n";

    private static final Map<String, Scripted> CACHE = new ConcurrentHashMap<>();
    private static final Scripted INVALID = new Scripted(null, null);

    private RailScriptRenderers() {
    }

    public static Scripted get(RailDefinition def) {
        if (def == null || def.getScriptPath() == null || def.getScriptPath().isBlank()) {
            return null;
        }
        Scripted s = CACHE.computeIfAbsent(def.getId(), id -> create(def));
        return s == INVALID ? null : s;
    }

    private static Scripted create(RailDefinition def) {
        try {
            String source = RailPackLoader.readScriptContent(def);
            if (source == null || source.isBlank()) {
                return INVALID;
            }
            ScriptEngine se = ScriptUtil.doScript(PRELUDE + source);
            Object rcName = se.get("renderClass");
            if (rcName == null) {
                return INVALID;
            }
            Class<?> rc = Class.forName(rcName.toString(), true, ScriptUtil.class.getClassLoader());
            Object instance = rc.getDeclaredConstructor().newInstance();
            if (!(instance instanceof RailPartsRenderer renderer)) {
                //車両用等は対象外 (RailPartsRenderer のみ)
                return INVALID;
            }
            renderer.setScript(se);
            se.put("renderer", renderer);
            ModelObject modelObject = new ModelObject(new TextureSet[]{new TextureSet(new Material(null))});
            renderer.init(null, modelObject);
            RealTrainModUnofficial.LOGGER.info("Rail script renderer initialized: {} ({})", def.getId(), rcName);
            return new Scripted(renderer, se);
        } catch (Throwable t) {
            RealTrainModUnofficial.LOGGER.warn("Failed to init rail script renderer for {}", def.getId(), t);
            return INVALID;
        }
    }

    public static final class Scripted {
        private final RailPartsRenderer renderer;
        private final ScriptEngine engine;
        private final Map<BlockPos, GLRecorder> staticCache = new HashMap<>();
        private final Map<String, Set<String>> singleGroupCache = new HashMap<>();

        Scripted(RailPartsRenderer renderer, ScriptEngine engine) {
            this.renderer = renderer;
            this.engine = engine;
        }

        /**
         * スクリプト静的描画 + 本家デフォルト配置 (フィルタ済) を記録し、PoseStack に再生する。
         *
         * @return true = このレンダラが描画を担当した (通常パイプライン不要)
         */
        public boolean render(TileEntityLargeRailCore be, RailMap[] maps, float partialTick, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay,
                              MqoModelLoader.MqoModel model) {
            if (be instanceof TileEntityLargeRailSwitchCore) {
                //分岐は本家スクリプトも早期 return する (renderRailDynamic 側)。
                //暫定: 既存の分岐パイプラインに任せる。
                return false;
            }
            BlockPos pos = be.getBlockPos();
            GLRecorder rec = this.staticCache.get(pos);
            if (rec == null || be.shouldRerenderRail) {
                rec = new GLRecorder();
                GLRecorder.activate(rec);
                try {
                    //① スクリプト自前描画 (renderRailStatic) — メイン + subRails
                    int railCount = 1 + be.subRails.size();
                    for (int i = 0; i < railCount; i++) {
                        this.renderer.currentRailIndex = i;
                        this.renderer.renderRailStatic(be, 0.0D, 0.0D, 0.0D, partialTick, 0);
                    }
                    //② 本家デフォルト配置 (shouldRenderObject でオブジェクト毎フィルタ)
                    this.recordDefaultPlacement(be, maps, model, rec);
                } catch (Throwable t) {
                    RealTrainModUnofficial.LOGGER.warn("Rail script render failed at {}", pos, t);
                } finally {
                    GLRecorder.deactivate();
                    this.renderer.currentRailIndex = 0;
                }
                this.staticCache.put(pos, rec);
                be.shouldRerenderRail = false;
            }
            this.replay(rec, poseStack, buffer, packedLight, packedOverlay, model);
            return true;
        }

        /**
         * 本家 RailPartsRenderer のデフォルトレール描画:
         * split = length*2 (0.5m 毎)、各点でモデルを yaw/-pitch/roll 回転して設置。
         * 各オブジェクトは shouldRenderObject(tile, objName, max, i) が true のときのみ。
         */
        private void recordDefaultPlacement(TileEntityLargeRailCore be, RailMap[] maps,
                                            MqoModelLoader.MqoModel model, GLRecorder rec) {
            BlockPos origin = be.getBlockPos();
            Set<String> groupNames = model.getOriginalGroupNames();
            for (RailMap map : maps) {
                if (map == null) {
                    continue;
                }
                double length = map.getLength();
                int max = (int) Math.floor(length * 2.0D);
                if (max < 1) {
                    max = 1;
                }
                //オブジェクト毎の許可判定 (この定義のスクリプトは名前でのみ判定するのが通例。
                //位置依存スクリプトに備え、両端 (0, max) と中央で判定して OR を取る)
                Set<String> allowed = new LinkedHashSet<>();
                for (String name : groupNames) {
                    if (this.shouldRender(be, name, max, 0)
                            || this.shouldRender(be, name, max, max / 2)
                            || this.shouldRender(be, name, max, max)) {
                        allowed.add(name.trim().toLowerCase(Locale.ROOT));
                    }
                }
                if (allowed.isEmpty()) {
                    continue;
                }
                for (int i = 0; i <= max; i++) {
                    double[] p1 = map.getRailPos(max, i);
                    double h = map.getRailHeight(max, i);
                    float yaw = map.getRailYaw(max, i);
                    float pitch = map.getRailPitch(max, i);
                    float roll = map.getRailRoll(max, i);

                    float relX = (float) (p1[1] - origin.getX());
                    float relY = (float) (h - origin.getY() - 0.0625D);
                    float relZ = (float) (p1[0] - origin.getZ());

                    rec.push();
                    rec.translate(relX, relY, relZ);
                    rec.rotate(yaw, 0.0F, 1.0F, 0.0F);
                    rec.rotate(-pitch, 1.0F, 0.0F, 0.0F);
                    rec.rotate(roll, 0.0F, 0.0F, 1.0F);
                    rec.renderGroups(allowed);
                    rec.pop();
                }
            }
        }

        private boolean shouldRender(TileEntityLargeRailCore be, String objName, double len, double pos) {
            return this.renderer.shouldRenderObject(be, objName, len, pos);
        }

        @SuppressWarnings("unchecked")
        private void replay(GLRecorder rec, PoseStack poseStack, MultiBufferSource buffer,
                            int packedLight, int packedOverlay, MqoModelLoader.MqoModel model) {
            int light = packedLight;
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
                        Vector3f axis = new Vector3f(cmd.b, cmd.c, cmd.d);
                        if (axis.lengthSquared() > 1.0e-6F) {
                            axis.normalize();
                            poseStack.mulPose(new org.joml.Quaternionf().rotationAxis(cmd.a * Mth.DEG_TO_RAD, axis));
                        }
                    }
                    case SCALE -> poseStack.scale(cmd.a, cmd.b, cmd.c);
                    case BRIGHTNESS -> light = (int) cmd.a;
                    case COLOR -> {
                        //TODO: カラーオーバーレイ対応 (現状のレールスクリプトでは未使用)
                    }
                    case RENDER_PARTS -> {
                        Set<String> names = this.singleGroupCache.computeIfAbsent(cmd.name,
                                n -> Set.of(n.trim().toLowerCase(Locale.ROOT)));
                        model.renderNamedGroups(poseStack, buffer, light, packedOverlay, false, names, null);
                    }
                    case RENDER_GROUPS -> {
                        if (cmd.payload instanceof Set<?> names) {
                            model.renderNamedGroups(poseStack, buffer, light, packedOverlay, false, (Set<String>) names, null);
                        }
                    }
                }
            }
            //スクリプトの push/pop 不整合を補正
            while (depth > 0) {
                poseStack.popPose();
                depth--;
            }
        }
    }
}
