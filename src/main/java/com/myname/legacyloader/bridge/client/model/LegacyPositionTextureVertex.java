package com.myname.legacyloader.bridge.client.model;

import com.myname.legacyloader.bridge.util.LegacyVec3;

public class LegacyPositionTextureVertex {
    public LegacyVec3 vector3D;
    public float texturePositionX;
    public float texturePositionY;

    public LegacyPositionTextureVertex(float x, float y, float z, float textureX, float textureY) {
        this(LegacyVec3.createVectorHelper(x, y, z), textureX, textureY);
    }

    public LegacyPositionTextureVertex(LegacyPositionTextureVertex vertex, float textureX, float textureY) {
        this.vector3D = vertex.vector3D;
        this.texturePositionX = textureX;
        this.texturePositionY = textureY;
    }

    public LegacyPositionTextureVertex(LegacyVec3 vector, float textureX, float textureY) {
        this.vector3D = vector;
        this.texturePositionX = textureX;
        this.texturePositionY = textureY;
    }

    public LegacyPositionTextureVertex setTexturePosition(float textureX, float textureY) {
        return new LegacyPositionTextureVertex(this, textureX, textureY);
    }

    public LegacyPositionTextureVertex func_78240_a(float textureX, float textureY) {
        return setTexturePosition(textureX, textureY);
    }
}
