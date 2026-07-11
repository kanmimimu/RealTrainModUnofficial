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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本家式レール描画システム (Phase 3 先行 / レール描画の作り直し)。
 *
 * 本家アーキテクチャの忠実再現:
 * - スクリプト付き: renderRailStatic(tile,x,y,z,pt,pass) をスクリプトが実行。
 *   デフォルト配置はスクリプトが renderer.renderStaticParts(...) を呼んだ時のみ
 *   (shouldRenderObject を位置ごとに通す = 端トリミング/枕木循環)。
 * - スクリプト無し: renderStaticParts 相当のデフォルト配置のみ。
 * - GL 呼び出しは GLRecorder に記録し BE ごとにキャッシュ (本家 DisplayList 相当)。
 * - 分岐コアのみ既存パイプラインにフォールバック (トング/ポイント描画は未移植)。
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

    /**
     * スクリプト無しレール用の素の RailPartsRenderer (shouldRenderObject 常に true)。
     */
    private static final RailPartsRenderer PLAIN = new RailPartsRenderer();
    private static final Map<BlockPos, GLRecorder> PLAIN_CACHE = new HashMap<>();

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
                RealTrainModUnofficial.LOGGER.warn("Rail script not readable for {} ({})", def.getId(), def.getScriptPath());
                return INVALID;
            }
            ScriptEngine se = ScriptUtil.doScript(PRELUDE + source);
            Object rcName = se.get("renderClass");
            if (rcName == null) {
                RealTrainModUnofficial.LOGGER.warn("Rail script has no renderClass: {}", def.getId());
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

    /**
     * スクリプト無しレールの本家デフォルト描画 (作り直し後の標準パス)。
     *
     * @return true = 描画を担当した
     */
    public static boolean renderPlain(TileEntityLargeRailCore be, RailMap[] maps, PoseStack poseStack,
                                      MultiBufferSource buffer, int packedLight, int packedOverlay,
                                      MqoModelLoader.MqoModel model) {
        if (be instanceof TileEntityLargeRailSwitchCore) {
            return false;
        }
        BlockPos pos = be.getBlockPos();
        GLRecorder rec = PLAIN_CACHE.get(pos);
        if (rec == null || be.shouldRerenderRail) {
            rec = new GLRecorder();
            GLRecorder.activate(rec);
            try {
                PLAIN.modelGroupNames = model.getOriginalGroupNames();
                PLAIN.renderStaticParts(be, 0.0D, 0.0D, 0.0D);
            } catch (Throwable t) {
                RealTrainModUnofficial.LOGGER.warn("Plain rail render failed at {}", pos, t);
            } finally {
                GLRecorder.deactivate();
            }
            PLAIN_CACHE.put(pos, rec);
            be.shouldRerenderRail = false;
        }
        replay(rec, poseStack, buffer, packedLight, packedOverlay, model);
        return true;
    }

    public static final class Scripted {
        private final RailPartsRenderer renderer;
        private final ScriptEngine engine;
        private final Map<BlockPos, GLRecorder> staticCache = new HashMap<>();

        Scripted(RailPartsRenderer renderer, ScriptEngine engine) {
            this.renderer = renderer;
            this.engine = engine;
        }

        /**
         * スクリプト描画 (renderRailStatic → 内部で renderStaticParts) を記録・再生する。
         *
         * @return true = このレンダラが描画を担当した
         */
        public boolean render(TileEntityLargeRailCore be, RailMap[] maps, float partialTick, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay,
                              MqoModelLoader.MqoModel model) {
            if (be instanceof TileEntityLargeRailSwitchCore) {
                //分岐トング/ポイント描画は既存パイプラインへ (renderRailDynamic 未移植)
                return false;
            }
            BlockPos pos = be.getBlockPos();
            GLRecorder rec = this.staticCache.get(pos);
            if (rec == null || be.shouldRerenderRail) {
                rec = new GLRecorder();
                GLRecorder.activate(rec);
                try {
                    this.renderer.modelGroupNames = model.getOriginalGroupNames();
                    int railCount = 1 + be.subRails.size();
                    for (int i = 0; i < railCount; i++) {
                        this.renderer.currentRailIndex = i;
                        this.renderer.renderRailStatic(be, 0.0D, 0.0D, 0.0D, partialTick, 0);
                    }
                } catch (Throwable t) {
                    RealTrainModUnofficial.LOGGER.warn("Rail script render failed at {}", pos, t);
                } finally {
                    GLRecorder.deactivate();
                    this.renderer.currentRailIndex = 0;
                }
                this.staticCache.put(pos, rec);
                be.shouldRerenderRail = false;
            }
            replay(rec, poseStack, buffer, packedLight, packedOverlay, model);
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private static void replay(GLRecorder rec, PoseStack poseStack, MultiBufferSource buffer,
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
                    //カラーオーバーレイは現状のレールスクリプトでは未使用
                }
                case RENDER_PARTS -> {
                    Set<String> names = Set.of(cmd.name.trim().toLowerCase(Locale.ROOT));
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
