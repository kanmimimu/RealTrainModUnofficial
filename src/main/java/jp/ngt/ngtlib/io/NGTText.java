package jp.ngt.ngtlib.io;

import java.util.ArrayList;
import java.util.List;

/**
 * 本家 NGTLib jp.ngt.ngtlib.io.NGTText の移植 (スクリプトが触る範囲)。
 *
 * <p>パックのスクリプトは {@code Packages.jp.ngt.ngtlib.io.NGTText} で直接参照してくる。
 * クラスが無いと {@code NGTText.readText is not a function} で落ちる (ログに 505 件)。
 * 実ファイルの読み書きはパック側の想定と噛み合わないので、空を返す安全な実装にする。
 */
public final class NGTText {

    private NGTText() {
    }

    public static List<String> readText(Object resource) {
        return new ArrayList<>();
    }

    public static List<String> readTextLines(Object resource) {
        return new ArrayList<>();
    }

    public static String loadText(Object resource) {
        return "";
    }

    public static String createText(Object... args) {
        return "";
    }

    public static void writeText(Object... args) {
    }

    public static void appendText(Object... args) {
    }

    public static String applyTextStyles(Object... args) {
        return args != null && args.length > 0 ? String.valueOf(args[0]) : "";
    }
}
