package jp.ngt.rtm.modelpack.state;

import java.util.function.Supplier;

/**
 * 本家 jp.ngt.rtm.modelpack.state.ResourceState のスクリプト互換最小移植。
 * スクリプトは getDataMap() / getResourceName() を使う。
 */
public class ResourceState {
    public int color;

    private final DataMap dataMap = new DataMap();
    private final Supplier<String> nameSupplier;

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
}
