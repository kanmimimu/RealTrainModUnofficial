package jp.ngt.mccompat;

/**
 * パックスクリプト互換: 1.12 SRG の Minecraft.func_71410_x().func_110434_K() 系。
 * (RTMCore.VERSION に "1.7.10" を含むため通常は旧パスが使われるが、保険として提供)
 */
public final class Minecraft {
    private static final Minecraft INSTANCE = new Minecraft();
    private static final TextureManagerCompat TEXTURE_MANAGER = new TextureManagerCompat();

    private Minecraft() {
    }

    /**
     * getMinecraft
     */
    public static Minecraft func_71410_x() {
        return INSTANCE;
    }

    /**
     * getTextureManager
     */
    public TextureManagerCompat func_110434_K() {
        return TEXTURE_MANAGER;
    }

    public static final class TextureManagerCompat {
        /**
         * register (name, DynamicTexture) → ResourceLocation
         */
        public Object func_110578_a(String name, Object dynamicTexture) {
            if (dynamicTexture instanceof DynamicTexture dyn && dyn.image != null) {
                int handle = TextureUtil.func_110996_a();
                TextureUtil.func_110987_a(handle, dyn.image);
                return TextureUtil.getTexture(handle);
            }
            return null;
        }

        /**
         * bindTexture
         */
        public void func_110577_a(Object rl) {
            jp.ngt.ngtlib.util.NGTUtilClient.bindTexture(rl);
        }
    }
}
