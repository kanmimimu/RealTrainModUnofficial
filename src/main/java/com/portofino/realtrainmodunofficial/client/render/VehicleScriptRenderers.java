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
import jp.ngt.rtm.render.ModelObject;
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

    private static final String PRELUDE =
            "var GL11 = Java.type('jp.ngt.ngtlib.renderer.GL11Facade');\n" +
            "var GL12 = GL11;\n" +
            "var MathHelper = Java.type('jp.ngt.mccompat.MathHelper');\n";

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^\\s*//include\\s*<([^>]+)>", Pattern.MULTILINE);

    private static final String[][] FQN_REMAP = {
            {"Packages.net.minecraft.util.ResourceLocation", "Packages.jp.ngt.mccompat.ResourceLocation"},
            {"Packages.net.minecraft.client.renderer.texture.TextureUtil", "Packages.jp.ngt.mccompat.TextureUtil"},
            {"Packages.net.minecraft.client.renderer.texture.DynamicTexture", "Packages.jp.ngt.mccompat.DynamicTexture"},
            {"Packages.net.minecraft.client.Minecraft", "Packages.jp.ngt.mccompat.Minecraft"},
            {"Packages.net.minecraft.util.math.BlockPos", "Packages.net.minecraft.core.BlockPos"},
            {"Packages.net.minecraft.world.EnumSkyBlock", "Packages.jp.ngt.mccompat.EnumSkyBlock"},
            {"Packages.net.minecraft.util.MathHelper", "Packages.jp.ngt.mccompat.MathHelper"},
            {"Packages.net.minecraft.util.math.MathHelper", "Packages.jp.ngt.mccompat.MathHelper"},
    };

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
            source = resolveIncludes(source, new HashSet<>());
            source = remapLegacyClasses(source);

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

    private static String resolveIncludes(String source, Set<String> visited) {
        Matcher m = INCLUDE_PATTERN.matcher(source);
        StringBuilder includes = new StringBuilder();
        while (m.find()) {
            String path = m.group(1).trim();
            if (!visited.add(path.toLowerCase(Locale.ROOT))) {
                continue;
            }
            byte[] bytes = NGTFileLoader.findAsset(path);
            if (bytes == null) {
                RealTrainModUnofficial.LOGGER.warn("Vehicle script include not found: {}", path);
                continue;
            }
            String text = decode(bytes);
            includes.append(resolveIncludes(text, visited)).append('\n');
        }
        return includes + source;
    }

    private static String decode(byte[] bytes) {
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (utf8.indexOf('�') >= 0) {
            return new String(bytes, java.nio.charset.Charset.forName("Shift_JIS"));
        }
        return utf8;
    }

    private static String remapLegacyClasses(String source) {
        String out = source;
        for (String[] pair : FQN_REMAP) {
            out = out.replace(pair[0], pair[1]);
        }
        return out;
    }

    public static final class Scripted {
        private final VehiclePartsRenderer renderer;
        private final ScriptEngine engine;
        private final ModelObject modelObject;
        private boolean warnedRenderFail;

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
        public boolean render(EntityTrainBase entity, float partialTick, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay,
                              MqoModelLoader.MqoModel bodyModel) {
            GLRecorder rec = new GLRecorder();
            GLRecorder.activate(rec);
            try {
                this.renderer.currentMatId = 0;
                this.renderer.render(entity, 0, partialTick);
            } catch (Throwable t) {
                if (!this.warnedRenderFail) {
                    this.warnedRenderFail = true;
                    RealTrainModUnofficial.LOGGER.warn("Vehicle script render failed", t);
                }
            } finally {
                GLRecorder.deactivate();
            }
            if (rec.isEmpty()) {
                return false;
            }
            replay(rec, poseStack, buffer, packedLight, packedOverlay, bodyModel,
                    this.modelObject != null ? this.modelObject.model : null);
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private static void replay(GLRecorder rec, PoseStack poseStack, MultiBufferSource buffer,
                               int packedLight, int packedOverlay, MqoModelLoader.MqoModel model,
                               PolygonModel bodyGraph) {
        int light = packedLight;
        ResourceLocation overrideTex = null;
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
                case BRIGHTNESS -> light = (int) cmd.a;
                case COLOR -> {
                    //TODO カラーオーバーレイ
                }
                case BIND_TEXTURE -> overrideTex = cmd.payload instanceof ResourceLocation rl ? rl : null;
                case RENDER_PARTS, RENDER_GROUPS -> {
                    if (cmd.payload instanceof Set<?> names) {
                        if (overrideTex != null && bodyGraph != null) {
                            //テクスチャ差し替え中 (発光/ヘッドライト等): モデルグラフから
                            //同グループの面を差し替えテクスチャで描画 (UV は MQO のまま)
                            for (Object name : names) {
                                drawModelGroup(bodyGraph, String.valueOf(name), poseStack, buffer,
                                        light, packedOverlay, overrideTex);
                            }
                        } else if (model != null) {
                            //translucent=false は全バッチ描画 (renderSelectedBatches のフィルタ仕様)
                            model.renderNamedGroups(poseStack, buffer, light, packedOverlay, false, (Set<String>) names, null);
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
                        drawModelGroup(pm, cmd.name, poseStack, buffer, light, packedOverlay, overrideTex);
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
        vc.addVertex(mat, v[o], v[o + 1], v[o + 2])
                .setColor(v[o + 5], v[o + 6], v[o + 7], v[o + 8])
                .setUv(v[o + 3], v[o + 4])
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, normal.x, normal.y, normal.z);
    }

    private static void drawModelGroup(PolygonModel pm, String groupName, PoseStack poseStack,
                                       MultiBufferSource buffer, int light, int overlay, ResourceLocation texture) {
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
        //本家はブレンド有効の即時描画 (モニタ/発光パーツ)
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(tex));
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
                    vc.addVertex(mat, vert.x, vert.y, vert.z)
                            .setColor(255, 255, 255, 255)
                            .setUv(u, vv)
                            .setOverlay(overlay)
                            .setLight(light)
                            .setNormal(pose, normal.x, normal.y, normal.z);
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
