package jp.ngt.rtm.render;

import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore;

/**
 * 本家 jp.ngt.rtm.render.RailPartsRenderer の移植。
 * レールスクリプトの renderRailStatic/renderRailDynamic/shouldRenderObject を仲介する。
 * currentRailIndex はスクリプトがリフレクションで読むため、このクラスに直接宣言する (本家準拠)。
 */
public class RailPartsRenderer extends PartsRenderer {
    /**
     * 現在描画中のレール index (0=メイン, 1..=subRails)。スクリプトがリフレクション参照。
     */
    public int currentRailIndex;

    public boolean isSwitchRail(Object tile) {
        return tile instanceof TileEntityLargeRailSwitchCore;
    }

    public void renderRailStatic(TileEntityLargeRailCore tile, double x, double y, double z, float partialTicks, int pass) {
        if (this.script != null) {
            ScriptUtil.doScriptIgnoreError(this.script, "renderRailStatic", tile, x, y, z, partialTicks, pass);
        }
    }

    public void renderRailDynamic(TileEntityLargeRailCore tile, double x, double y, double z, float partialTicks, int pass) {
        if (this.script != null) {
            ScriptUtil.doScriptIgnoreError(this.script, "renderRailDynamic", tile, x, y, z, partialTicks, pass);
        }
    }

    /**
     * 通常パイプラインの各オブジェクトを描画するかどうか (端のトリミング等)。
     */
    public boolean shouldRenderObject(TileEntityLargeRailCore tile, String objName, double len, double pos) {
        if (this.script == null) {
            return true;
        }
        Object result = ScriptUtil.doScriptIgnoreError(this.script, "shouldRenderObject", tile, objName, len, pos);
        if (result instanceof Boolean b) {
            return b;
        }
        return true;
    }
}
