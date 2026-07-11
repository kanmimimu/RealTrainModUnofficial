package jp.ngt.rtm.render;

import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.GLRecorder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.script.ScriptEngine;

/**
 * 本家 jp.ngt.rtm.render.PartsRenderer の段階的移植 (レールスクリプトが使う面から)。
 * GL 呼び出し・Parts 描画は GLRecorder に記録され、BER 側で PoseStack に再生される。
 * TODO(Phase 3 続き): 車両系ヘルパー (getMCTime/getData/setData/renderLightEffect 等) の拡充。
 */
public class PartsRenderer {
    protected ScriptEngine script;
    protected ModelObject modelObject = new ModelObject(null);

    public void setScript(ScriptEngine se) {
        this.script = se;
    }

    public ScriptEngine getScript() {
        return this.script;
    }

    public void init(Object modelSet, Object modelObject) {
        if (modelObject instanceof ModelObject mo) {
            this.modelObject = mo;
        }
        if (this.script != null) {
            ScriptUtil.doScriptIgnoreError(this.script, "init", modelSet, this.modelObject);
        }
    }

    public ModelObject getModelObject() {
        return this.modelObject;
    }

    public Parts registerParts(Parts par1) {
        return par1;
    }

    /**
     * Parts.render() から呼ばれる (GLRecorder への記録)。
     */
    public void recordRenderParts(String objName) {
        GLRecorder r = GLRecorder.active();
        if (r != null) {
            r.renderParts(objName);
        }
    }

    public void bindTexture(Object texture) {
        //モデルのテクスチャはパイプラインが束縛済み。記録不要。
    }

    /**
     * 本家: world.getLightBrightnessForSkyBlocks 相当のパック輝度。
     */
    public int getBrightness(Object world, double x, double y, double z) {
        if (world instanceof Level level) {
            BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
            return net.minecraft.client.renderer.LevelRenderer.getLightColor(level, pos);
        }
        return 0xF000F0;
    }

    public void setBrightness(int packedLight) {
        GLRecorder r = GLRecorder.active();
        if (r != null) {
            r.brightness(packedLight);
        }
    }

    public Level getWorld(Object tile) {
        if (tile instanceof BlockEntity be) {
            return be.getLevel();
        }
        return Minecraft.getInstance().level;
    }

    public int getX(Object tile) {
        return tile instanceof BlockEntity be ? be.getBlockPos().getX() : 0;
    }

    public int getY(Object tile) {
        return tile instanceof BlockEntity be ? be.getBlockPos().getY() : 0;
    }

    public int getZ(Object tile) {
        return tile instanceof BlockEntity be ? be.getBlockPos().getZ() : 0;
    }

    public double sigmoid(double x, double c) {
        return NGTMath.sigmoid(x, c);
    }

    public void debug(String msg) {
        jp.ngt.ngtlib.io.NGTLog.debug(msg);
    }
}
