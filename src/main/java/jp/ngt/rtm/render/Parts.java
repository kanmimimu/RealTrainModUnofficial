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

    public Parts(String... names) {
        this.names = names != null ? names : new String[0];
        this.nameSet = new LinkedHashSet<>(Arrays.asList(this.names));
    }

    public String[] getNames() {
        return this.names;
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
