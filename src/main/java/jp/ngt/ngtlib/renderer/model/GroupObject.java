package jp.ngt.ngtlib.renderer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 本家 jp.ngt.ngtlib.renderer.model.GroupObject のスクリプト互換移植。
 */
public class GroupObject {
    public final String name;
    public final List<Face> faces = new ArrayList<>();

    public GroupObject(String name) {
        this.name = name == null ? "" : name;
    }

    public String getName() {
        return this.name;
    }
}
