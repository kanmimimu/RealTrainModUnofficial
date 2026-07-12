package jp.ngt.rtm.render;

import com.portofino.realtrainmodunofficial.blockentity.InstalledObjectBlockEntity;
import com.portofino.realtrainmodunofficial.installedobject.InstalledObjectCategory;
import jp.ngt.ngtlib.io.ScriptUtil;

/**
 * 本家 jp.ngt.rtm.render.MachinePartsRenderer (KaizPatchX) の移植。
 * 踏切等の設置物スクリプト (RenderCrossingGate01.js 等) が renderClass に指定する。
 */
public class MachinePartsRenderer extends TileEntityPartsRenderer {

    public MachinePartsRenderer(String... par1) {
        super(par1);
    }

    /**
     * 本家 render: スクリプトの render(tileEntity, pass, partialTick) を実行
     */
    public void render(Object t, int pass, float partialTick) {
        this.currentPass = pass;
        if (this.script != null) {
            ScriptUtil.doScriptIgnoreError(this.script, "render", t, pass, partialTick);
        }
    }

    /**
     * 本家: 踏切=barMoveCount/90 (0..1)、改札=通行可否
     */
    public float getMovingCount(Object tile) {
        if (tile instanceof InstalledObjectBlockEntity be) {
            if (be.getCategory() == InstalledObjectCategory.CROSSING) {
                return (float) be.getBarMoveCount() / 90.0F;
            }
            if (be.getCategory() == InstalledObjectCategory.TICKET_GATE) {
                //本家 MachinePartsRenderer: canThrough() ? 0 : 1 — 通れる(開)=0, 閉=1。
                //MQO のデフォルトポーズが「開」で、state>0 の回転で「閉」になる。
                return be.isTicketGateOpen() ? 0.0F : 1.0F;
            }
        }
        return 0.0F;
    }

    /**
     * 本家: -1:OFF, 0 or 1:ON (踏切の交互点滅) / 照明は電源状態
     */
    public int getLightState(Object tile) {
        if (tile instanceof InstalledObjectBlockEntity be) {
            if (be.getCategory() == InstalledObjectCategory.CROSSING) {
                return be.getLightCount();
            }
            if (be.getCategory() == InstalledObjectCategory.LIGHT) {
                return be.isPowered() ? 1 : -1;
            }
        }
        return -1;
    }

    public int getLodState(Object tile) {
        return 0;
    }

    /**
     * BER 側で回転適用済みのため 0 (本家は TE の向きをここで返す)
     */
    public float getPitch(Object tile) {
        return 0.0F;
    }

    public float getYaw(Object tile) {
        return 0.0F;
    }
}
