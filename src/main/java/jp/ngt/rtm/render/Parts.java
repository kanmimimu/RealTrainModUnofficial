package jp.ngt.rtm.render;

/**
 * 本家 jp.ngt.rtm.render.Parts の移植 (レールスクリプトが使う面のみ)。
 * スクリプト: pf = renderer.registerParts(new Parts("PF_01")); pf.render(renderer);
 */
public class Parts {
    public final String name;

    public Parts(String name) {
        this.name = name;
    }

    public void render(PartsRenderer renderer) {
        renderer.recordRenderParts(this.name);
    }

    public void render(Object renderer) {
        if (renderer instanceof PartsRenderer pr) {
            pr.recordRenderParts(this.name);
        }
    }
}
