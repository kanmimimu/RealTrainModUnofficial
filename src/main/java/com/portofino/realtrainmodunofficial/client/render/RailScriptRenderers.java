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
import jp.ngt.rtm.render.ModelObject;
import jp.ngt.rtm.render.PartsRenderer;
import jp.ngt.rtm.render.RailPartsRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * レール定義ごとの本家式スクリプトレンダラ (jp.ngt.rtm.render.RailPartsRenderer) の管理。
 * Phase 3 先行実装: 本物の Nashorn + 実 jp.ngt クラスでレールスクリプト
 * (renderRailStatic / shouldRenderObject) を実行し、GL 呼び出しを GLRecorder 経由で
 * PoseStack に再生する。renderRailStatic は本家の DisplayList 同様 BE ごとに記録キャッシュ。
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
        private final Map<String, Set<String>> groupNameCache = new HashMap<>();
        private final Map<String, Boolean> shouldRenderCache = new HashMap<>();

        Scripted(RailPartsRenderer renderer, ScriptEngine engine) {
            this.renderer = renderer;
            this.engine = engine;
        }

        /**
         * 通常パイプラインの objName を描画するか (本家 shouldRenderObject)。
         */
        public boolean shouldRenderObject(TileEntityLargeRailCore be, String objName, double len, double pos) {
            //Advanced Rails 等は定数を返すため定義単位でキャッシュ
            Boolean cached = this.shouldRenderCache.get(objName);
            if (cached != null) {
                return cached;
            }
            boolean result = this.renderer.shouldRenderObject(be, objName, len, pos);
            this.shouldRenderCache.put(objName, result);
            return result;
        }

        /**
         * 通常パイプラインを全てスキップするか (probe が false → 全 objName 非描画とみなす)。
         */
        public boolean skipsNormalPipeline(TileEntityLargeRailCore be) {
            return !this.shouldRenderObject(be, "___rtmu_probe___", 0.0D, 0.0D);
        }

        /**
         * renderRailStatic を記録 (初回 or 再描画要求時) して PoseStack に再生する。
         */
        public void renderStatic(TileEntityLargeRailCore be, float partialTick, PoseStack poseStack,
                                 MultiBufferSource buffer, int packedLight, int packedOverlay,
                                 MqoModelLoader.MqoModel model) {
            BlockPos pos = be.getBlockPos();
            GLRecorder rec = this.staticCache.get(pos);
            if (rec == null || be.shouldRerenderRail) {
                rec = new GLRecorder();
                GLRecorder.activate(rec);
                try {
                    int railCount = 1 + be.subRails.size();
                    for (int i = 0; i < railCount; i++) {
                        this.renderer.currentRailIndex = i;
                        this.renderer.renderRailStatic(be, 0.0D, 0.0D, 0.0D, partialTick, 0);
                    }
                } finally {
                    GLRecorder.deactivate();
                    this.renderer.currentRailIndex = 0;
                }
                this.staticCache.put(pos, rec);
                be.shouldRerenderRail = false;
            }
            this.replay(rec, poseStack, buffer, packedLight, packedOverlay, model);
        }

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
                        Set<String> names = this.groupNameCache.computeIfAbsent(cmd.name,
                                n -> Set.of(n.trim().toLowerCase(Locale.ROOT)));
                        model.renderNamedGroups(poseStack, buffer, light, packedOverlay, false, names, null);
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
