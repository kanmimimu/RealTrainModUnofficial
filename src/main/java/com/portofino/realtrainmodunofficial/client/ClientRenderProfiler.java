package com.portofino.realtrainmodunofficial.client;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 軽量なクライアント側描画プロファイラ。
 * 1 秒単位で描画カテゴリごとの合計時間を集計して、必要な時だけ HUD に出す。
 */
@EventBusSubscriber(modid = RealTrainModUnofficial.MODID, value = Dist.CLIENT)
public final class ClientRenderProfiler {
    private static final String[] CATEGORY_NAMES = {"Rail", "RailOld", "Train", "Object"};
    private static final int CATEGORY_RAIL = 0;
    private static final int CATEGORY_TRAIN = 2;
    /** 分岐など、統合メッシュに乗らず毎フレーム逐次描画しているレール。 */
    private static final int CATEGORY_RAIL_OLD = 1;
    private static final int CATEGORY_OBJECT = 3;

    private static final long[] totalsNs = new long[CATEGORY_NAMES.length];
    private static final int[] counts = new int[CATEGORY_NAMES.length];
    private static final long[] displayTotalsNs = new long[CATEGORY_NAMES.length];
    private static final int[] displayCounts = new int[CATEGORY_NAMES.length];

    //CPU が 1 秒間に VertexConsumer へ書き込んだ頂点数 / バッチ数。
    //「重さの正体は頂点スループット」なのかを数字で確定させるための計測。
    private static long verticesThisSecond;
    private static long batchesThisSecond;
    private static long displayVertices;
    private static long displayBatches;
    private static int framesThisSecond;
    private static int displayFrames;

    //レールが「統合メッシュ (1本=1VBO)」で描けているか、それとも逐次描画に落ちているか。
    //レールが重いときの切り分け用: fallback が多ければ焼き込みが効いていない。
    private static int railMergedThisSecond;
    private static int railFallbackThisSecond;
    private static int railBakeThisSecond;
    //GL ステートを切り替えた回数と VBO を描いた回数。重さは「描画回数」より
    //「ステート切替の回数」で決まるので、両方を出して効果を見る。
    private static int railStateSetupThisSecond;
    private static int railVboDrawThisSecond;
    private static int displayRailMerged;
    private static int displayRailFallback;
    private static int displayRailBake;
    private static int displayRailStateSetup;
    private static int displayRailVboDraw;

    public static void countRailStateSetup() {
        railStateSetupThisSecond++;
    }

    public static void countRailVboDraw() {
        railVboDrawThisSecond++;
    }

    public static void countRailMerged() {
        railMergedThisSecond++;
    }

    public static void countRailFallback() {
        railFallbackThisSecond++;
    }

    public static void countRailBake() {
        railBakeThisSecond++;
    }

    private static long lastSnapshotNs = System.nanoTime();
    private static boolean overlayEnabled;

    /**
     * 1 バッチ (= 1 回の描画呼び出し) 分の頂点数を計上する。
     */
    public static void addVertices(int vertexCount) {
        verticesThisSecond += vertexCount;
        batchesThisSecond++;
    }

    public static void countFrame() {
        framesThisSecond++;
    }

    private ClientRenderProfiler() {
    }

    public static void toggleOverlay() {
        overlayEnabled = !overlayEnabled;
    }

    public static long begin() {
        return System.nanoTime();
    }

    public static void endRailOld(long startNs) {
        record(CATEGORY_RAIL_OLD, startNs);
    }

    public static void endRail(long startNs) {
        record(CATEGORY_RAIL, startNs);
    }

    public static void endTrain(long startNs) {
        record(CATEGORY_TRAIN, startNs);
    }

    public static void endInstalledObject(long startNs) {
        record(CATEGORY_OBJECT, startNs);
    }

    private static synchronized void record(int category, long startNs) {
        long elapsed = System.nanoTime() - startNs;
        totalsNs[category] += elapsed;
        counts[category]++;
        snapshotIfNeeded();
    }

    private static void snapshotIfNeeded() {
        long now = System.nanoTime();
        if (now - lastSnapshotNs < 1_000_000_000L) {
            return;
        }
        System.arraycopy(totalsNs, 0, displayTotalsNs, 0, totalsNs.length);
        System.arraycopy(counts, 0, displayCounts, 0, counts.length);
        for (int i = 0; i < totalsNs.length; i++) {
            totalsNs[i] = 0L;
            counts[i] = 0;
        }
        displayVertices = verticesThisSecond;
        displayBatches = batchesThisSecond;
        displayFrames = framesThisSecond;
        displayRailMerged = railMergedThisSecond;
        displayRailFallback = railFallbackThisSecond;
        displayRailBake = railBakeThisSecond;
        displayRailStateSetup = railStateSetupThisSecond;
        displayRailVboDraw = railVboDrawThisSecond;
        verticesThisSecond = 0L;
        batchesThisSecond = 0L;
        framesThisSecond = 0;
        railMergedThisSecond = 0;
        railFallbackThisSecond = 0;
        railBakeThisSecond = 0;
        railStateSetupThisSecond = 0;
        railVboDrawThisSecond = 0;
        lastSnapshotNs = now;

        //画面のオーバーレイと同じ内容をログにも残す。
        //スクリーンショットを撮らなくても、ログを見れば負荷の内訳が分かるようにする。
        if (overlayEnabled) {
            logSnapshot();
        }
    }

    private static void logSnapshot() {
        StringBuilder sb = new StringBuilder("[Profiler]");
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            double totalMs = displayTotalsNs[i] / 1_000_000.0D;
            double avgMs = displayCounts[i] > 0 ? totalMs / displayCounts[i] : 0.0D;
            sb.append(String.format(java.util.Locale.ROOT, " %s=%.2fms/%.3favg(%d)",
                CATEGORY_NAMES[i], totalMs, avgMs, displayCounts[i]));
        }
        long vertsPerFrame = displayFrames > 0 ? displayVertices / displayFrames : 0L;
        int setupPerFrame = displayFrames > 0 ? displayRailStateSetup / displayFrames : 0;
        int drawPerFrame = displayFrames > 0 ? displayRailVboDraw / displayFrames : 0;
        sb.append(String.format(java.util.Locale.ROOT,
            " | fps=%d verts/f=%d | railMesh merged=%d fallback=%d bake=%d state/f=%d vbo/f=%d",
            displayFrames, vertsPerFrame,
            displayRailMerged, displayRailFallback, displayRailBake, setupPerFrame, drawPerFrame));
        com.portofino.realtrainmodunofficial.RealTrainModUnofficial.LOGGER.info(sb.toString());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !overlayEnabled) {
            return;
        }

        countFrame();
        snapshotIfNeeded();

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = mc.font;
        int x = 8;
        int y = 8;
        int lineHeight = font.lineHeight + 2;
        int width = 0;
        String[] lines = new String[CATEGORY_NAMES.length + 3];
        lines[0] = "Profiler [F8]";
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            double totalMs = displayTotalsNs[i] / 1_000_000.0D;
            double avgMs = displayCounts[i] > 0 ? totalMs / displayCounts[i] : 0.0D;
            lines[i + 1] = CATEGORY_NAMES[i] + ": "
                + String.format(java.util.Locale.ROOT, "%.2f ms", totalMs)
                + " / "
                + String.format(java.util.Locale.ROOT, "%.2f avg", avgMs)
                + " (" + displayCounts[i] + ")";
        }
        //CPU が毎フレーム VertexConsumer に流している頂点数 (= 重さの正体)
        long vertsPerFrame = displayFrames > 0 ? displayVertices / displayFrames : 0L;
        long batchesPerFrame = displayFrames > 0 ? displayBatches / displayFrames : 0L;
        int setupPerFrame = displayFrames > 0 ? displayRailStateSetup / displayFrames : 0;
        int drawPerFrame = displayFrames > 0 ? displayRailVboDraw / displayFrames : 0;
        lines[CATEGORY_NAMES.length + 2] = "RailMesh: merged " + displayRailMerged
            + " / fallback " + displayRailFallback
            + " / bake " + displayRailBake
            + "  (state " + setupPerFrame + "/f, vbo " + drawPerFrame + "/f)";
        lines[CATEGORY_NAMES.length + 1] = "Verts/frame: " + vertsPerFrame
            + "  (batches " + batchesPerFrame + ", fps " + displayFrames + ")";

        for (String line : lines) {
            width = Math.max(width, font.width(line));
        }

        graphics.fill(x - 4, y - 4, x + width + 6, y + lineHeight * lines.length + 2, 0x90000000);
        for (int i = 0; i < lines.length; i++) {
            graphics.drawString(font, lines[i], x, y + i * lineHeight, 0xFFFFFF, false);
        }
    }
}
