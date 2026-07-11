package jp.ngt.mccompat;

/**
 * パックスクリプト互換: 1.12 SRG の Minecraft.func_71410_x().func_110434_K() 系。
 * (RTMCore.VERSION に "1.7.10" を含むため通常は旧パスが使われるが、保険として提供)
 */
public final class Minecraft {
    private static final Minecraft INSTANCE = new Minecraft();
    private static final TextureManagerCompat TEXTURE_MANAGER = new TextureManagerCompat();
    private static final LanguageManagerCompat LANGUAGE_MANAGER = new LanguageManagerCompat();

    /** currentScreen (SRG)。refresh() で更新。null なら GUI 非表示。 */
    public Object field_71462_r;
    /** thePlayer (SRG)。refresh() で更新 (PlayerCompat ラッパー)。 */
    public PlayerCompat field_71439_g;
    /** theWorld (SRG)。refresh() で更新。 */
    public WorldCompat field_71441_e;

    private Minecraft() {
    }

    /**
     * getMinecraft
     */
    public static Minecraft func_71410_x() {
        return INSTANCE;
    }

    public static Minecraft getInstanceCompat() {
        return INSTANCE;
    }

    /**
     * クライアントの毎フレーム/毎tick 呼び出しで現在値を反映する
     * (CarRenderer / クライアント tick イベントから)。
     */
    public static void refresh() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            INSTANCE.field_71462_r = mc.screen;
            INSTANCE.field_71439_g = mc.player != null ? PlayerCompat.of(mc.player) : null;
            if (INSTANCE.field_71439_g != null) {
                INSTANCE.field_71439_g.refresh();
            }
            if (mc.level != null && (INSTANCE.field_71441_e == null || INSTANCE.field_71441_e.getLevel() != mc.level)) {
                INSTANCE.field_71441_e = new WorldCompat(mc.level);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * func_135016_M = getLanguageManager
     */
    public LanguageManagerCompat func_135016_M() {
        return LANGUAGE_MANAGER;
    }

    public static final class LanguageManagerCompat {
        /** func_135041_c = getCurrentLanguage */
        public LanguageCompat func_135041_c() {
            return new LanguageCompat();
        }
    }

    public static final class LanguageCompat {
        /** func_135034_a = getLanguageCode */
        public String func_135034_a() {
            try {
                return net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
            } catch (Throwable t) {
                return "en_us";
            }
        }
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
