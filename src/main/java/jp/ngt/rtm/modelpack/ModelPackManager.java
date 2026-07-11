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

    public String getScript(String path) {
        byte[] bytes = jp.ngt.ngtlib.io.NGTFileLoader.findAsset(path);
        return bytes != null ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : null;
    }
}
