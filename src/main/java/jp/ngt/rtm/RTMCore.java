package jp.ngt.rtm;

/**
 * 本家 jp.ngt.rtm.RTMCore の段階的移植。
 * VERSION はパックスクリプトが isLegacy() 判定 (indexOf("1.7.10")) に使うため
 * "1.7.10" を含む文字列にする (legacy パス = RailProperty API を使わせる)。
 */
public final class RTMCore {
    public static final String VERSION = "1.7.10.41 KaizPatchX/remaster-1.20.1";

    private RTMCore() {
    }
}
