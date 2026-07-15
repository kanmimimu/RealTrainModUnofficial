package jp.ngt.rtm.modelpack;

/**
 * 本家 jp.ngt.rtm.modelpack.ModelPackManager のスクリプト互換最小移植。
 * スクリプトは INSTANCE.getResource(domain, path) を呼ぶ。
 * TODO(Phase 4): 型システム (registerType/registerModelset) の本実装。
 */
public final class ModelPackManager {
    public static final ModelPackManager INSTANCE = new ModelPackManager();

    private ModelPackManager() {
    }

    public jp.ngt.mccompat.ResourceLocation getResource(String domain, String path) {
        return new jp.ngt.mccompat.ResourceLocation(domain, path);
    }

    /**
     * 本家 getResource(String): "domain:path" を分割 (既定 domain=minecraft)。
     * スクリプトの自前 include (eval(NGTText.readText(INSTANCE.getResource(path)))) が
     * 単一引数で呼ぶため必須。無いと TypeError で include が全滅し、依存スクリプト
     * (render_function.js 等) が読まれず MCVersionChecker 未定義などに連鎖する。
     */
    public jp.ngt.mccompat.ResourceLocation getResource(String path) {
        String domain = "minecraft";
        if (path != null && path.contains(":")) {
            String[] sa = path.split(":", 2);
            domain = sa[0];
            path = sa[1];
        }
        return getResource(domain, path);
    }

    public String getScript(String path) {
        byte[] bytes = jp.ngt.ngtlib.io.NGTFileLoader.findAsset(path);
        return bytes != null ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : null;
    }
}
