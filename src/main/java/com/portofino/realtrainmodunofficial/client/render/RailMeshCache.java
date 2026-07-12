package com.portofino.realtrainmodunofficial.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import com.portofino.realtrainmodunofficial.client.model.MqoModelLoader;
import jp.ngt.ngtlib.renderer.GLRecorder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * レール 1 本ぶんの描画を「1 つの統合メッシュ (VBO)」に焼いて使い回す。
 *
 * 本家 RTM 1.7.10 はレール 1 本をディスプレイリストに 1 回だけ焼き、以後は GPU 側に
 * 置きっぱなしにしていた。1.21 移植ではそれが「毎フレーム、0.5m 刻みの位置ごとに
 * モデルを描画呼び出し」になっており、50m のレール 1 本で 900 回以上の draw call
 * (setupRenderState + ユニフォーム転送 + bind/draw) を毎フレーム発行していた
 * (実測 2.83ms/本 = レール 10 本で 28ms/フレーム)。
 *
 * ここでは既存の描画コードに頂点を吐かせて (MeshCapture) それを 1 つにまとめ、
 * 毎フレームは RenderType ごとに 1 回だけ描く。頂点の中身も RenderType の選択も
 * 既存コードが決めたものをそのまま使うため、見た目は変わらない。
 */
public final class RailMeshCache {

    /**
     * 1 本のレールが統合メッシュ化できる頂点数の上限 (異常に巨大なレール対策)。
     * 超えた場合は従来の逐次描画にフォールバックする。
     */
    private static final long MAX_MERGED_VERTICES = 4_000_000L;

    /**
     * 同時に保持するレールメッシュ数の上限 (VRAM 上限)。超えたら古いものから解放する。
     */
    private static final int MAX_ENTRIES = 512;

    private static final Map<BlockPos, RailMesh> CACHE = new LinkedHashMap<>(64, 0.75F, true);

    private static Level lastLevel;

    private RailMeshCache() {
    }

    private record RailMesh(List<MeshCapture.Section> sections, MqoModelLoader.MqoModel model, int light) {
        void close() {
            for (MeshCapture.Section section : sections) {
                section.close();
            }
        }
    }

    /**
     * 統合メッシュで 1 本を描く。
     *
     * @param rebuilt true = 記録 (GLRecorder) が作り直された → メッシュも焼き直す
     * @return true = 描画した (呼び出し元は逐次描画をスキップする)
     */
    public static boolean draw(BlockPos pos, GLRecorder rec, MqoModelLoader.MqoModel model,
                               PoseStack poseStack, int packedLight, int packedOverlay, boolean rebuilt) {
        if (rec == null || model == null || rec.isEmpty()) {
            return false;
        }
        dropIfLevelChanged();

        RailMesh mesh = CACHE.get(pos);
        //モデルが差し替わった (パック再読込 / キャッシュ追い出し) 場合と、
        //ライト値が変わった (頂点にライトを焼いているため) 場合も焼き直す。
        if (rebuilt || mesh == null || mesh.model() != model || mesh.light() != packedLight) {
            RailMesh old = CACHE.remove(pos);
            if (old != null) {
                old.close();
            }
            mesh = bake(rec, model, packedLight, packedOverlay);
            if (mesh == null) {
                return false;
            }
            CACHE.put(pos, mesh);
            trim();
        }

        for (MeshCapture.Section section : mesh.sections()) {
            if (!MqoModelLoader.MqoModel.drawMergedVbo(section.vbo(), section.renderType(), poseStack)) {
                //描けなかった (VBO 無効等) → このレールは統合メッシュを諦めて逐次描画に戻す
                CACHE.remove(pos);
                mesh.close();
                return false;
            }
        }
        return true;
    }

    /**
     * 記録済みの描画コマンドを再生し、既存コードが吐いた頂点をそのまま 1 つの VBO に焼く。
     * 単位行列の PoseStack で再生するので、頂点はレール原点 (ブロック隅) 基準になる。
     * 毎フレームの描画時に本物の pose を掛けるため、結果は逐次描画と数学的に同じ。
     */
    private static RailMesh bake(GLRecorder rec, MqoModelLoader.MqoModel model, int packedLight, int packedOverlay) {
        MeshCapture.Source source = new MeshCapture.Source();
        boolean prevCapture = MqoModelLoader.captureMode;
        MqoModelLoader.captureMode = true;
        try {
            RailScriptRenderers.replayForCapture(rec, new PoseStack(), source, packedLight, packedOverlay, model);
        } catch (Throwable t) {
            RealTrainModUnofficial.LOGGER.warn("Rail mesh bake failed", t);
            return null;
        } finally {
            MqoModelLoader.captureMode = prevCapture;
        }

        long vertices = source.totalVertices();
        if (vertices <= 0L || vertices > MAX_MERGED_VERTICES) {
            return null;
        }
        try {
            List<MeshCapture.Section> sections = source.upload();
            if (sections.isEmpty()) {
                return null;
            }
            return new RailMesh(sections, model, packedLight);
        } catch (Throwable t) {
            RealTrainModUnofficial.LOGGER.warn("Rail mesh upload failed", t);
            return null;
        }
    }

    private static void trim() {
        while (CACHE.size() > MAX_ENTRIES) {
            Iterator<Map.Entry<BlockPos, RailMesh>> it = CACHE.entrySet().iterator();
            if (!it.hasNext()) {
                return;
            }
            Map.Entry<BlockPos, RailMesh> eldest = it.next();
            it.remove();
            eldest.getValue().close();
        }
    }

    /**
     * ワールドが変わったら全部解放する (VRAM を持ち越さない)。
     */
    private static void dropIfLevelChanged() {
        Level level = Minecraft.getInstance().level;
        if (level != lastLevel) {
            clear();
            lastLevel = level;
        }
    }

    public static void clear() {
        for (RailMesh mesh : CACHE.values()) {
            mesh.close();
        }
        CACHE.clear();
    }

    /**
     * レールが壊された / 作り直された時に呼ぶ (VBO を解放する)。
     */
    public static void invalidate(BlockPos pos) {
        RailMesh mesh = CACHE.remove(pos);
        if (mesh != null) {
            mesh.close();
        }
    }
}
