package jp.ngt.ngtlib.renderer.model;

/**
 * 本家 jp.ngt.ngtlib.renderer.model.Face のスクリプト互換移植。
 * vertices は面の頂点 (四角形は4点)。uvs は頂点順の {u,v}。
 */
public class Face {
    public final Vertex[] vertices;
    /**
     * 頂点順の u,v (長さ vertices.length*2)。UV 無しは null。
     */
    public final float[] uvs;
    public final int materialId;
    public Vertex faceNormal;

    public Face(Vertex[] vertices, float[] uvs, int materialId) {
        this.vertices = vertices;
        this.uvs = uvs;
        this.materialId = materialId;
    }

    public void calculateFaceNormal(VecAccuracy accuracy) {
        if (this.vertices.length < 3) {
            this.faceNormal = new Vertex(0.0F, 1.0F, 0.0F);
            return;
        }
        Vertex v0 = this.vertices[0];
        Vertex v1 = this.vertices[1];
        Vertex v2 = this.vertices[2];
        float ax = v1.x - v0.x, ay = v1.y - v0.y, az = v1.z - v0.z;
        float bx = v2.x - v0.x, by = v2.y - v0.y, bz = v2.z - v0.z;
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0e-7F) {
            this.faceNormal = new Vertex(0.0F, 1.0F, 0.0F);
        } else {
            this.faceNormal = new Vertex(nx / len, ny / len, nz / len);
        }
    }
}
