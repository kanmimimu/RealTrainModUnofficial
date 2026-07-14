package jp.ngt.rtm.modelpack.state;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 本家 jp.ngt.rtm.modelpack.state.ResourceState のスクリプト互換最小移植。
 * スクリプトは getDataMap() / getResourceName() / add|removeExclusionParts() を使う。
 */
public class ResourceState {
    public int color;

    private final DataMap dataMap = new DataMap();
    private final Supplier<String> nameSupplier;

    /**
     * 本家 ResourceState.exclusionParts: 「今は描かないパーツ」の名前。
     * <p>
     * RTM 標準スクリプトはドアの開閉をこれで表現する。ドアが開いた側の扉パーツ
     * (door_fl / door_bl …) を除外リストに入れて、閉じたら外す。除外されたグループは
     * モデル描画から落ちるので、扉が消える = 開いて見える。
     * <p>
     * 描画スレッドが読み、スクリプト(同じ描画スレッド)が書くが、
     * サーバースクリプトからも触られうるので並行セットにしておく。
     * 照合は正規化 (trim + 小文字) 済みの名前で行う (モデル側のグループ名も同じ正規化)。
     */
    private final Set<String> exclusionParts = ConcurrentHashMap.newKeySet();

    public ResourceState(Supplier<String> nameSupplier) {
        this.nameSupplier = nameSupplier;
    }

    public DataMap getDataMap() {
        return this.dataMap;
    }

    public String getResourceName() {
        return this.nameSupplier.get();
    }

    public String getName() {
        return this.getResourceName();
    }

    /** 本家 ResourceState.addExclusionParts */
    public void addExclusionParts(String... names) {
        if (names == null) {
            return;
        }
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                this.exclusionParts.add(normalize(name));
            }
        }
    }

    /** 本家 ResourceState.removeExclusionParts */
    public void removeExclusionParts(String... names) {
        if (names == null) {
            return;
        }
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                this.exclusionParts.remove(normalize(name));
            }
        }
    }

    public void clearExclusionParts() {
        this.exclusionParts.clear();
    }

    /**
     * @return 除外中のパーツ名 (正規化済み)。空なら除外なし。
     */
    public Set<String> getExclusionParts() {
        return this.exclusionParts.isEmpty() ? Set.of() : Collections.unmodifiableSet(this.exclusionParts);
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
