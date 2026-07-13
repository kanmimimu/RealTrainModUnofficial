package jp.ngt.ngtlib.util;

import jp.ngt.ngtlib.renderer.GLRecorder;
import net.minecraft.client.Minecraft;

/**
 * 本家 jp.ngt.ngtlib.util.NGTUtilClient のスクリプト互換ファサード。
 * bindTexture は GLRecorder に BIND_TEXTURE として記録され、
 * 以降の描画 (parts.render / tessellator / model.renderPart) にテクスチャ差し替えとして効く。
 */
@SuppressWarnings("unused")
public final class NGTUtilClient {
    private NGTUtilClient() {
    }

    /**
     * スクリプト互換の Minecraft ラッパーを返す (field_71462_r/field_71439_g 等の
     * SRG フィールドを SRB3/NGTO Builder が直接読むため)。最新値へ refresh してから返す。
     */
    public static jp.ngt.mccompat.Minecraft getMinecraft() {
        jp.ngt.mccompat.Minecraft.refresh();
        return jp.ngt.mccompat.Minecraft.func_71410_x();
    }

    public static Minecraft getRealMinecraft() {
        return Minecraft.getInstance();
    }

    /**
     * クライアントプレイヤー。{@link NGTUtil#getClientPlayer()} からリフレクションで呼ばれる。
     * <p>
     * {@code NGTUtil} 側に置くと、戻り値の型と {@code Minecraft.player} (LocalPlayer) の
     * 代入互換性を JVM の検証器が確かめるために LocalPlayer を読み込み、専用サーバーで
     * NGTUtil ごとロードできなくなる。クライアント専用のこちらに置くこと。
     */
    public static net.minecraft.world.entity.player.Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    /** クライアントワールド。{@link NGTUtil#getClientWorld()} からリフレクションで呼ばれる。 */
    public static Object getClientWorld() {
        net.minecraft.world.level.Level level = Minecraft.getInstance().level;
        return level != null ? new jp.ngt.mccompat.WorldCompat(level) : null;
    }

    /**
     * mccompat.ResourceLocation / 実 ResourceLocation の両方を受ける。
     * null でデフォルトテクスチャへ復帰。
     */
    public static void bindTexture(Object texture) {
        GLRecorder r = GLRecorder.active();
        if (r == null) {
            return;
        }
        if (texture == null) {
            r.bindTexture(null);
        } else if (texture instanceof net.minecraft.resources.ResourceLocation rl) {
            r.bindTexture(rl);
        } else if (texture instanceof jp.ngt.mccompat.ResourceLocation compat) {
            r.bindTexture(resolve(compat));
        }
    }

    /**
     * スクリプトが作る RL はパック内アセットを指すことが多い。
     * パックアセットとして解決済みならその動的テクスチャ、そうでなければ実 RL をそのまま返す。
     */
    private static net.minecraft.resources.ResourceLocation resolve(jp.ngt.mccompat.ResourceLocation compat) {
        net.minecraft.resources.ResourceLocation packTex =
                jp.ngt.ngtlib.io.NGTFileLoader.resolvePackTexture(compat.func_110623_a());
        return packTex != null ? packTex : compat.toReal();
    }

    public static boolean usingShader() {
        //Iris/Oculus のシェーダーパック使用中か (スクリプトが発光の描き方を切り替える)
        return com.portofino.realtrainmodunofficial.client.ShaderCompat.isShaderPackInUse();
    }
}
