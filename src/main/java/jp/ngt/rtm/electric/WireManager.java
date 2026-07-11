package jp.ngt.rtm.electric;

import com.portofino.realtrainmodunofficial.blockentity.InstalledObjectBlockEntity;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 本家 ElectricalWiringManager/WireManager 相当 (簡略忠実版)。
 * ワイヤー (InstalledObject WIRE) の両端をエッジとして接続グラフを保持し、
 * 入力コネクタ/変換器からの信号を BFS で伝播する。
 */
public final class WireManager {
    private static final Map<Level, Map<BlockPos, Set<BlockPos>>> NETWORKS = new WeakHashMap<>();

    private WireManager() {
    }

    public static synchronized void register(Level level, BlockPos a, BlockPos b) {
        if (level == null || a == null || b == null) {
            return;
        }
        Map<BlockPos, Set<BlockPos>> adj = NETWORKS.computeIfAbsent(level, l -> new HashMap<>());
        adj.computeIfAbsent(a.immutable(), p -> new HashSet<>()).add(b.immutable());
        adj.computeIfAbsent(b.immutable(), p -> new HashSet<>()).add(a.immutable());
    }

    public static synchronized void unregister(Level level, BlockPos a, BlockPos b) {
        Map<BlockPos, Set<BlockPos>> adj = NETWORKS.get(level);
        if (adj == null || a == null || b == null) {
            return;
        }
        Set<BlockPos> sa = adj.get(a);
        if (sa != null) {
            sa.remove(b);
        }
        Set<BlockPos> sb = adj.get(b);
        if (sb != null) {
            sb.remove(a);
        }
    }

    /**
     * 信号伝播 (本家 propagateSignal)。origin から接続グラフ全体へ。
     * 変換器 (Increment/Decrement) を通過する信号はレベルが変換される。
     */
    public static void propagate(Level level, BlockPos origin, int signalLevel) {
        if (level == null || level.isClientSide) {
            return;
        }
        Map<BlockPos, Set<BlockPos>> adj;
        synchronized (WireManager.class) {
            adj = NETWORKS.get(level);
        }
        if (adj == null) {
            return;
        }
        Set<BlockPos> visited = new HashSet<>();
        Deque<Object[]> queue = new ArrayDeque<>();
        queue.add(new Object[]{origin.immutable(), signalLevel});
        visited.add(origin.immutable());
        int guard = 0;
        while (!queue.isEmpty() && guard++ < 4096) {
            Object[] entry = queue.poll();
            BlockPos pos = (BlockPos) entry[0];
            int lvl = (Integer) entry[1];

            int outLevel = applyAndTransform(level, pos, lvl, pos.equals(origin));

            Set<BlockPos> next = adj.get(pos);
            if (next == null) {
                continue;
            }
            for (BlockPos n : next) {
                if (visited.add(n)) {
                    queue.add(new Object[]{n, outLevel});
                }
            }
        }
    }

    /**
     * ノードへ信号を適用し、通過後のレベルを返す (変換器のみ変換)。
     */
    private static int applyAndTransform(Level level, BlockPos pos, int lvl, boolean isOrigin) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntitySignalConverter converter) {
            if (!isOrigin) {
                converter.setElectricity(pos.getX(), pos.getY(), pos.getZ(), lvl);
            }
            return switch (converter.getConverterType()) {
                case Increment -> Mth.clamp(lvl + 1, 0, 15);
                case Decrement -> Mth.clamp(lvl - 1, 0, 15);
                default -> lvl;
            };
        }
        if (be instanceof InstalledObjectBlockEntity io && !isOrigin) {
            if (io.getCategory() == InstalledObjectCategory.CONNECTOR_INPUT
                    || io.getCategory() == InstalledObjectCategory.CONNECTOR_OUTPUT) {
                io.setElectricity(lvl);
            }
        }
        return lvl;
    }
}
