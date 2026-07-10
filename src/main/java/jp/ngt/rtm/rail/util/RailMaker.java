package jp.ngt.rtm.rail.util;

import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本家 jp.ngt.rtm.rail.util.RailMaker (KaizPatchX) の忠実移植。
 * fixRTMRailMapVersion >= 1 の N字分岐は SwitchTypeSingleCrossFixRTMV1 を使用 (KaizPatchX 準拠)。
 */
public final class RailMaker {
    // see RailMapBasic.fixRTMRailMapVersion
    public final int fixRTMRailMapVersion;
    private final Level worldObj;
    private final List<RailPosition> rpList;

    /**
     * @deprecated use {@link #RailMaker(Level, List, int)}
     */
    @Deprecated
    public RailMaker(Level world, List<RailPosition> par2) {
        this(world, par2, 0);
    }

    public RailMaker(Level world, List<RailPosition> par2, int fixRTMRailMapVersion) {
        this.worldObj = world;
        this.rpList = par2;
        this.fixRTMRailMapVersion = fixRTMRailMapVersion;
    }

    /**
     * @deprecated use {@link #RailMaker(Level, RailPosition[], int)}
     */
    @Deprecated
    public RailMaker(Level world, RailPosition[] par2) {
        this(world, par2, 0);
    }

    public RailMaker(Level world, RailPosition[] par2, int fixRTMRailMapVersion) {
        this(world, new ArrayList<>(Arrays.asList(par2)), fixRTMRailMapVersion);
    }

    // ===== Remaster 暫定互換コンストラクタ (旧コードは world を渡さない。Phase 1 の BE 差し替え後に削除予定) =====

    /**
     * @deprecated Remaster 独自。本家に存在しない。
     */
    @Deprecated
    public RailMaker(List<RailPosition> positions) {
        this(null, new ArrayList<>(positions), 0);
    }

    /**
     * @deprecated Remaster 独自。本家に存在しない。
     */
    @Deprecated
    public RailMaker(RailPosition[] positions) {
        this(null, positions, 0);
    }

    private SwitchType getSwitchType() {
        if (this.rpList.size() == 3) {
            int i0 = this.rpList.stream().mapToInt(rp -> (rp.switchType == 1) ? 1 : 0).sum();

            if (i0 == 1) {
                return new SwitchType.SwitchBasic(fixRTMRailMapVersion);
            }
        } else if (this.rpList.size() == 4) {
            int i0 = this.rpList.stream().mapToInt(rp -> (rp.switchType == 1) ? 1 : 0).sum();

            if (i0 == 2) {
                if (fixRTMRailMapVersion >= 1) {
                    return new SwitchTypeSingleCrossFixRTMV1(fixRTMRailMapVersion);
                } else {
                    return new SwitchType.SwitchSingleCross(fixRTMRailMapVersion);
                }
            } else if (i0 == 4) {
                for (int i = 0; i < this.rpList.size(); ++i) {
                    for (int j = i + 1; j < this.rpList.size(); ++j)//全組み合わせ(重複なし)
                    {
                        if (this.rpList.get(i).direction == this.rpList.get(j).direction) {
                            return new SwitchType.SwitchScissorsCross(fixRTMRailMapVersion);
                        }
                    }
                }
                return new SwitchType.SwitchDiamondCross(fixRTMRailMapVersion);
            }
        }

        return null;
    }

    public SwitchType getSwitch() {
        SwitchType type = this.getSwitchType();
        if (type != null) {
            List<RailPosition> switchList = new ArrayList<>();//分岐あり
            List<RailPosition> normalList = new ArrayList<>();//分岐なし
            this.rpList.forEach(rp -> (rp.switchType == 1 ? switchList : normalList).add(rp));

            if (type.init(switchList, normalList)) {
                return type;
            }
        }

        return null;
    }
}
