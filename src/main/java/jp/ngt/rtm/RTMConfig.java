package jp.ngt.rtm;

/**
 * 本家 jp.ngt.rtm.RTMConfig の段階的移植 (レール関連のみ)。
 * TODO: NeoForge Config への接続。既定値は本家と同一。
 */
public final class RTMConfig {
    /**
     * レール生成距離 (default:64, max:256)
     */
    public static short railGeneratingDistance = 64;
    /**
     * レール生成高さ (default:8, max:256)
     */
    public static short railGeneratingHeight = 8;

    public static short markerDisplayDistance = 100;

    private RTMConfig() {
    }
}
