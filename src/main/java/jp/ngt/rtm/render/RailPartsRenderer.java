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

    /**
     * 分岐コア用: renderStaticParts が描画する RailMap の差し替え。
     * getRailMap(null) は分岐で先頭マップしか返さないため、呼び出し側
     * (RailScriptRenderers) が getAllRailMaps の各マップを設定してマップごとに回す。
     */
    public jp.ngt.rtm.rail.util.RailMap renderMapOverride;

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

    /**
     * 描画対象モデルのオブジェクト名一覧 (renderStaticParts 用)。
     * クライアント側 (RailScriptRenderers) が呼出前に設定する。
     */
    public java.util.Set<String> modelGroupNames = java.util.Set.of();

    /**
     * 本家のデフォルトレール描画。スクリプトの renderRailStatic から
     * renderer.renderStaticParts(tileEntity, x, y, z) として呼ばれる。
     * split = length*2 (0.5m 毎)、各点でモデルを yaw/-pitch/roll 回転して設置し、
     * 各オブジェクトは shouldRenderObject(tile, objName, max, i) を通す (位置依存可)。
     */
    public void renderStaticParts(Object tileObj, double x, double y, double z) {
        if (!(tileObj instanceof TileEntityLargeRailCore tile)) {
            return;
        }
        jp.ngt.ngtlib.renderer.GLRecorder rec = jp.ngt.ngtlib.renderer.GLRecorder.active();
        if (rec == null) {
            return;
        }
        jp.ngt.rtm.rail.util.RailMap map = this.renderMapOverride != null
                ? this.renderMapOverride : tile.getRailMap(null);
        if (map == null) {
            return;
        }
        net.minecraft.core.BlockPos origin = tile.getBlockPos();
        double length = map.getLength();
        int max = (int) Math.floor(length * 2.0D);
        if (max < 1) {
            max = 1;
        }
        for (int i = 0; i <= max; i++) {
            java.util.Set<String> allowed = new java.util.LinkedHashSet<>();
            for (String name : this.modelGroupNames) {
                if (this.shouldRenderObject(tile, name, max, i)) {
                    allowed.add(name.trim().toLowerCase(java.util.Locale.ROOT));
                }
            }
            if (allowed.isEmpty()) {
                continue;
            }
            double[] p1 = map.getRailPos(max, i);
            double h = map.getRailHeight(max, i);
            float yaw = map.getRailYaw(max, i);
            float pitch = map.getRailPitch(max, i);
            float roll = map.getRailRoll(max, i);

            float relX = (float) (x + p1[1] - origin.getX());
            float relY = (float) (y + h - origin.getY() - 0.0625D);
            float relZ = (float) (z + p1[0] - origin.getZ());

            rec.push();
            rec.translate(relX, relY, relZ);
            rec.rotate(yaw, 0.0F, 1.0F, 0.0F);
            rec.rotate(-pitch, 1.0F, 0.0F, 0.0F);
            rec.rotate(roll, 0.0F, 0.0F, 1.0F);
            rec.renderGroups(allowed);
            rec.pop();
        }
    }
}
