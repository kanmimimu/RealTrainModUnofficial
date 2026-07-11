package jp.ngt.rtm.render;

import jp.ngt.ngtlib.renderer.model.TextureSet;

/**
 * 本家 jp.ngt.rtm.render.ModelObject の最小移植。
 * スクリプトは renderer.getModelObject().textures[0].material.texture を参照する。
 * TODO(Phase 4): IModelNGT/TextureSet 完全版に置換。
 */
public class ModelObject {
    public TextureSet[] textures;

    /**
     * 本家 IModelNGT 相当 (スクリプトは modelObj.model を Parts.getObjects に渡す)。
     * 実体は jp.ngt.ngtlib.renderer.model.PolygonModel。
     */
    public jp.ngt.ngtlib.renderer.model.PolygonModel model;

    public ModelObject(TextureSet[] textures) {
        this.textures = textures != null ? textures : new TextureSet[0];
    }
}
