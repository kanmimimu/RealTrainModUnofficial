package com.myname.legacyloader.bridge.client.resources;

import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public interface LegacyIResourceManager {
    LegacyResource getResource(ResourceLocation location) throws IOException;

    LegacyResource func_110536_a(ResourceLocation location) throws IOException;
}
