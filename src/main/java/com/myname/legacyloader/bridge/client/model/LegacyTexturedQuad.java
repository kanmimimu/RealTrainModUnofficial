package com.myname.legacyloader.bridge.client.model;

import com.myname.legacyloader.bridge.client.renderer.LegacyTessellator;
import com.myname.legacyloader.bridge.util.LegacyVec3;

public class LegacyTexturedQuad {
    public LegacyPositionTextureVertex[] vertexPositions;
    public int nVertices;
    private boolean invertNormal;

    public LegacyTexturedQuad(LegacyPositionTextureVertex[] vertices) {
        this.vertexPositions = vertices;
        this.nVertices = vertices.length;
    }

    public LegacyTexturedQuad(LegacyPositionTextureVertex[] vertices,
                              int minU, int minV, int maxU, int maxV,
                              float textureWidth, float textureHeight) {
        this(vertices);
        float uPad = 0.0F / textureWidth;
        float vPad = 0.0F / textureHeight;
        vertices[0] = vertices[0].setTexturePosition(maxU / textureWidth - uPad, minV / textureHeight + vPad);
        vertices[1] = vertices[1].setTexturePosition(minU / textureWidth + uPad, minV / textureHeight + vPad);
        vertices[2] = vertices[2].setTexturePosition(minU / textureWidth + uPad, maxV / textureHeight - vPad);
        vertices[3] = vertices[3].setTexturePosition(maxU / textureWidth - uPad, maxV / textureHeight - vPad);
    }

    public void flipFace() {
        LegacyPositionTextureVertex[] flipped = new LegacyPositionTextureVertex[this.vertexPositions.length];
        for (int i = 0; i < this.vertexPositions.length; i++) {
            flipped[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
        }
        this.vertexPositions = flipped;
    }

    public void func_78235_a() {
        flipFace();
    }

    public void draw(LegacyTessellator tessellator, float scale) {
        if (tessellator == null || vertexPositions == null || vertexPositions.length < 3) return;
        LegacyVec3 edgeA = subtract(vertexPositions[1].vector3D, vertexPositions[0].vector3D);
        LegacyVec3 edgeB = subtract(vertexPositions[1].vector3D, vertexPositions[2].vector3D);
        LegacyVec3 normal = normalize(cross(edgeB, edgeA));

        tessellator.startDrawingQuads();
        if (invertNormal) {
            tessellator.setNormal((float) -normal.x, (float) -normal.y, (float) -normal.z);
        } else {
            tessellator.setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        }

        for (int i = 0; i < Math.min(4, this.vertexPositions.length); i++) {
            LegacyPositionTextureVertex vertex = this.vertexPositions[i];
            tessellator.addVertexWithUV(
                    vertex.vector3D.x * scale,
                    vertex.vector3D.y * scale,
                    vertex.vector3D.z * scale,
                    vertex.texturePositionX,
                    vertex.texturePositionY);
        }
        tessellator.draw();
    }

    public void func_78236_a(LegacyTessellator tessellator, float scale) {
        draw(tessellator, scale);
    }

    private static LegacyVec3 subtract(LegacyVec3 a, LegacyVec3 b) {
        return new LegacyVec3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    private static LegacyVec3 cross(LegacyVec3 a, LegacyVec3 b) {
        return new LegacyVec3(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x);
    }

    private static LegacyVec3 normalize(LegacyVec3 value) {
        double length = Math.sqrt(value.x * value.x + value.y * value.y + value.z * value.z);
        if (length < 1.0E-4D) return new LegacyVec3(0, 1, 0);
        return new LegacyVec3(value.x / length, value.y / length, value.z / length);
    }
}
