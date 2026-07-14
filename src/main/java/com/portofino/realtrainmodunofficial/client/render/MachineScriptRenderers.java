package com.portofino.realtrainmodunofficial.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.blockentity.InstalledObjectBlockEntity;
import com.portofino.realtrainmodunofficial.client.model.MqoModelLoader;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectDefinition;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.renderer.GLRecorder;
import jp.ngt.ngtlib.renderer.model.Material;
import jp.ngt.ngtlib.renderer.model.ModelLoader;
import jp.ngt.ngtlib.renderer.model.PolygonModel;
import jp.ngt.ngtlib.renderer.model.TextureSet;
import jp.ngt.rtm.render.TileEntityPartsRenderer;
import jp.ngt.rtm.render.ModelObject;
import net.minecraft.client.renderer.MultiBufferSource;

import javax.script.ScriptEngine;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本家式の設置物 (踏切等) スクリプト描画。
 * ModelMachine_*.json の rendererPath (RenderCrossingGate01.js 等) を Nashorn で実行し、
 * renderClass (jp.ngt.rtm.render.MachinePartsRenderer) を毎フレーム記録→再生する。
 * pass 0 (通常) と pass 2 (発光: 警報灯) の 2 パスを本家どおり実行する。
 */
public final class MachineScriptRenderers {

    private static final Map<String, Scripted> CACHE = new ConcurrentHashMap<>();
    private static final Scripted INVALID = new Scripted(null, null);

    private MachineScriptRenderers() {
    }

    public static Scripted get(InstalledObjectDefinition def) {
        if (def == null || def.getScriptPath() == null || def.getScriptPath().isBlank()) {
            return null;
        }
        Scripted s = CACHE.computeIfAbsent(def.getId(), id -> create(def));
        return s == INVALID ? null : s;
    }

    private static Scripted create(InstalledObjectDefinition def) {
        try {
            byte[] bytes = NGTFileLoader.findAsset(def.getScriptPath());
            if (bytes == null) {
                RealTrainModUnofficial.LOGGER.warn("Machine script not found: {} ({})", def.getId(), def.getScriptPath());
                return INVALID;
            }
            String source = new String(bytes, StandardCharsets.UTF_8);

            ScriptEngine se = ScriptUtil.doScript(
                    "var GL11 = Java.type('jp.ngt.ngtlib.renderer.GL11Facade');\n" +
                    "var GL12 = GL11;\n" +
                    "var MathHelper = Java.type('jp.ngt.mccompat.MathHelper');\n" + source);
            Object rcName = se.get("renderClass");
            if (rcName == null) {
                return INVALID;
            }
            Class<?> rc = Class.forName(rcName.toString(), true, ScriptUtil.class.getClassLoader());
            Object instance;
            try {
                instance = rc.getConstructor(String[].class).newInstance(new Object[]{new String[0]});
            } catch (NoSuchMethodException e) {
                instance = rc.getDeclaredConstructor().newInstance();
            }
            //踏切/改札 (MachinePartsRenderer) に加え、信号 (SignalPartsRenderer) も
            //共通基底 TileEntityPartsRenderer なので受け入れる。
            if (!(instance instanceof TileEntityPartsRenderer renderer)) {
                RealTrainModUnofficial.LOGGER.warn("renderClass {} is not a TileEntityPartsRenderer ({})", rcName, def.getId());
                return INVALID;
            }
            renderer.setScript(se);
            se.put("renderer", renderer);

            //ModelObject: テクスチャ + モデルグラフ
            List<TextureSet> sets = new ArrayList<>();
            if (def.getTextureOverrides() != null) {
                for (String path : def.getTextureOverrides().values()) {
                    int meta = path.indexOf("|ptmeta=");
                    String clean = meta >= 0 ? path.substring(0, meta) : path;
                    sets.add(new TextureSet(new Material(new jp.ngt.mccompat.ResourceLocation("minecraft", clean))));
                }
            }
            if (sets.isEmpty()) {
                sets.add(new TextureSet(new Material(null)));
            }
            ModelObject mo = new ModelObject(sets.toArray(new TextureSet[0]));
            byte[] modelBytes = NGTFileLoader.findAsset("models/" + def.getModelFile());
            if (modelBytes == null) {
                modelBytes = NGTFileLoader.findAsset(def.getModelFile());
            }
            mo.model = modelBytes != null ? ModelLoader.parse(modelBytes, def.getModelFile()) : new PolygonModel();

            //getModelName() は本家 config.getName() 相当、つまり <b>素のモデル名</b>
            //("CrossingGate01R" / "Point01A") を返さなければならない。
            //以前は def.getId() ("crossing:pack名:CrossingGate01R") を入れていたため、
            //  RenderCrossingGate01.js : getModelName().equals("CrossingGate01R")
            //  RenderPoint01.js        : getModelName().equals("Point01A")
            //がどちらも常に false になり、右用の踏切が左用と同じ向きに描かれ、
            //自動転轍機がモーターでなくレバーで描かれていた。
            jp.ngt.rtm.modelpack.cfg.TrainConfig cfg = new jp.ngt.rtm.modelpack.cfg.TrainConfig();
            cfg.trainName = def.getDisplayName();
            cfg.init();
            renderer.init(new jp.ngt.rtm.modelpack.modelset.ModelSetCompat(cfg), mo);

            RealTrainModUnofficial.LOGGER.info("Machine script renderer initialized: {} ({})", def.getId(), rcName);
            return new Scripted(renderer, mo);
        } catch (Throwable t) {
            RealTrainModUnofficial.LOGGER.warn("Failed to init machine script renderer for {}", def.getId(), t);
            return INVALID;
        }
    }

    public static final class Scripted {
        private final TileEntityPartsRenderer renderer;
        private final ModelObject modelObject;

        Scripted(TileEntityPartsRenderer renderer, ModelObject modelObject) {
            this.renderer = renderer;
            this.modelObject = modelObject;
        }

        /**
         * @return true = 描画を担当した
         */
        public boolean render(InstalledObjectBlockEntity be, float partialTick, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay,
                              MqoModelLoader.MqoModel model) {
            GLRecorder rec = new GLRecorder();
            GLRecorder.activate(rec);
            try {
                this.renderer.currentMatId = 0;
                //本家: pass 0 (通常) → pass 2 (発光)。発光はフルブライトで描く。
                this.renderer.render(be, 0, partialTick);
                rec.brightness(0xF000F0);
                this.renderer.render(be, 2, partialTick);
            } finally {
                GLRecorder.deactivate();
            }
            if (rec.isEmpty()) {
                return false;
            }
            VehicleScriptRenderers.replay(rec, poseStack, buffer, packedLight, packedOverlay, model,
                    this.modelObject != null ? this.modelObject.model : null);
            return true;
        }
    }
}
