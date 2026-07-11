package jp.ngt.ngtlib.renderer.model;

import jp.ngt.ngtlib.renderer.GLRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 本家 jp.ngt.ngtlib.renderer.model.PolygonModel のスクリプト互換移植。
 * renderPart は GLRecorder に記録され、再生側が現在テクスチャで面を emit する。
 */
public class PolygonModel {
    public final List<GroupObject> groupObjects = new ArrayList<>();

    public GroupObject getGroupObject(String name) {
        for (GroupObject group : this.groupObjects) {
            if (group.name.equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    public List<GroupObject> getGroupObjects(String... names) {
        List<GroupObject> out = new ArrayList<>();
        for (String name : names) {
            GroupObject g = this.getGroupObject(name);
            if (g != null) {
                out.add(g);
            }
        }
        return out;
    }

    /**
     * 本家: renderPart(smoothing, objName) — 指定グループを即時描画。
     */
    public void renderPart(boolean smoothing, String objName) {
        GLRecorder rec = GLRecorder.active();
        if (rec != null && objName != null) {
            rec.drawModelGroup(this, objName.toLowerCase(Locale.ROOT));
        }
    }

    public void renderPart(String objName) {
        this.renderPart(false, objName);
    }

    /**
     * 全グループ描画。
     */
    public void renderAll() {
        GLRecorder rec = GLRecorder.active();
        if (rec != null) {
            for (GroupObject group : this.groupObjects) {
                rec.drawModelGroup(this, group.name.toLowerCase(Locale.ROOT));
            }
        }
    }
}
