package jp.ngt.ngtlib.renderer.model;

/**
 * 本家 jp.ngt.ngtlib.renderer.model.TextureSet の最小移植 (スクリプト可視フィールドのみ)。
 */
public class TextureSet {
    public Material material;

    public TextureSet(Material material) {
        this.material = material;
    }
}
