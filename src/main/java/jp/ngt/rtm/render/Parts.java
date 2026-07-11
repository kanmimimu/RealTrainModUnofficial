package jp.ngt.rtm.render;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 本家 jp.ngt.rtm.render.Parts の移植。
 * スクリプト用法:
 *   pf = renderer.registerParts(new Parts("PF_01"));            //単一
 *   ba = renderer.registerParts(new Parts("Ba1","Ba2",...));    //複数 (可変長)
 *   pf.render(renderer); ba.containsName(objName);
 */
public class Parts {
    private final String[] names;
    private final Set<String> nameSet;
    private jp.ngt.ngtlib.renderer.model.GroupObject[] objs;

    public Parts(String... names) {
        this.names = names != null ? names : new String[0];
        this.nameSet = new LinkedHashSet<>(Arrays.asList(this.names));
    }

    public String[] getNames() {
        return this.names;
    }

    /**
     * 本家 objNames フィールド互換 (スクリプトが直接参照する)。
     */
    public String[] getObjNames() {
        return this.names;
    }

    public void init(PartsRenderer renderer) {
    }

    /**
     * 本家 getObjects(IModelNGT) 互換 — PolygonModel からグループ取得 (CustomAnimator 等)。
     */
    public jp.ngt.ngtlib.renderer.model.GroupObject[] getObjects(Object model) {
        if (this.objs == null) {
            if (model instanceof jp.ngt.ngtlib.renderer.model.PolygonModel pm) {
                java.util.List<jp.ngt.ngtlib.renderer.model.GroupObject> found = new java.util.ArrayList<>();
                for (String name : this.names) {
                    for (jp.ngt.ngtlib.renderer.model.GroupObject obj : pm.groupObjects) {
                        if (name.equals(obj.name)) {
                            found.add(obj);
                            break;
                        }
                    }
                }
                this.objs = found.toArray(new jp.ngt.ngtlib.renderer.model.GroupObject[0]);
            } else {
                this.objs = new jp.ngt.ngtlib.renderer.model.GroupObject[0];
            }
        }
        return this.objs;
    }

    public boolean containsName(String objName) {
        return objName != null && this.nameSet.contains(objName);
    }

    public void render(PartsRenderer renderer) {
        for (String name : this.names) {
            renderer.recordRenderParts(name);
        }
    }

    public void render(Object renderer) {
        if (renderer instanceof PartsRenderer pr) {
            this.render(pr);
        }
    }
}
