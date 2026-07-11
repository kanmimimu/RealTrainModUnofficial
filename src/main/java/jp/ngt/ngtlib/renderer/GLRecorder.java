package jp.ngt.ngtlib.renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * スクリプトの GL11 呼び出しを記録し、PoseStack へ再生するためのオペコードバッファ。
 * (1.21 にイミディエイトモード GL が無いための本家 GL 呼び出し互換層)
 */
public final class GLRecorder {

    public enum Op {
        PUSH, POP, TRANSLATE, ROTATE, SCALE, COLOR, BRIGHTNESS, RENDER_PARTS, RENDER_GROUPS
    }

    public static final class Cmd {
        public final Op op;
        public final float a, b, c, d;
        public final String name;
        public final Object payload;

        Cmd(Op op, float a, float b, float c, float d, String name) {
            this(op, a, b, c, d, name, null);
        }

        Cmd(Op op, float a, float b, float c, float d, String name, Object payload) {
            this.op = op;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.name = name;
            this.payload = payload;
        }
    }

    private static final ThreadLocal<GLRecorder> ACTIVE = new ThreadLocal<>();

    private final List<Cmd> cmds = new ArrayList<>();

    public static GLRecorder active() {
        return ACTIVE.get();
    }

    public static void activate(GLRecorder recorder) {
        ACTIVE.set(recorder);
    }

    public static void deactivate() {
        ACTIVE.remove();
    }

    public void clear() {
        this.cmds.clear();
    }

    public List<Cmd> getCommands() {
        return this.cmds;
    }

    public boolean isEmpty() {
        return this.cmds.isEmpty();
    }

    public void push() {
        this.cmds.add(new Cmd(Op.PUSH, 0, 0, 0, 0, null));
    }

    public void pop() {
        this.cmds.add(new Cmd(Op.POP, 0, 0, 0, 0, null));
    }

    public void translate(float x, float y, float z) {
        this.cmds.add(new Cmd(Op.TRANSLATE, x, y, z, 0, null));
    }

    public void rotate(float deg, float x, float y, float z) {
        this.cmds.add(new Cmd(Op.ROTATE, deg, x, y, z, null));
    }

    public void scale(float x, float y, float z) {
        this.cmds.add(new Cmd(Op.SCALE, x, y, z, 0, null));
    }

    public void color(float r, float g, float b, float a) {
        this.cmds.add(new Cmd(Op.COLOR, r, g, b, a, null));
    }

    public void brightness(int packedLight) {
        this.cmds.add(new Cmd(Op.BRIGHTNESS, packedLight, 0, 0, 0, null));
    }

    public void renderParts(String objName) {
        this.cmds.add(new Cmd(Op.RENDER_PARTS, 0, 0, 0, 0, objName));
    }

    /**
     * 正規化済みグループ名 Set を一括描画 (デフォルトレール配置用)。
     */
    public void renderGroups(java.util.Set<String> normalizedNames) {
        this.cmds.add(new Cmd(Op.RENDER_GROUPS, 0, 0, 0, 0, null, normalizedNames));
    }
}
