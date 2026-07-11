package jp.ngt.ngtlib.renderer;

/**
 * 本家 jp.ngt.ngtlib.renderer.NGTRenderer のスクリプト互換 (最低限)。
 * NGTO Builder が NGTObject プレビュー描画に使うが、1.21 ではブロックの
 * 即時モード描画が無いため未対応 (設置機能自体は動く)。
 */
@SuppressWarnings("unused")
public final class NGTRenderer {
    private NGTRenderer() {
    }

    /** NGTObject のプレビュー描画 — TODO: 1.21 ブロック描画対応 */
    public static void renderNGTObject(Object ngto, boolean flag) {
    }
}
