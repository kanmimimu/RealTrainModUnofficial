package jp.ngt.ngtlib.renderer.model;

import jp.ngt.ngtlib.math.Vec3;

/**
 * 本家 jp.ngt.ngtlib.renderer.model.Vertex のスクリプト互換移植。
 */
public class Vertex {
    public final float x;
    public final float y;
    public final float z;

    public Vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public Vec3 toVec() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex other)) return false;
        return Float.compare(this.x, other.x) == 0
                && Float.compare(this.y, other.y) == 0
                && Float.compare(this.z, other.z) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Float.hashCode(this.x) + Float.hashCode(this.y)) + Float.hashCode(this.z);
    }
}
