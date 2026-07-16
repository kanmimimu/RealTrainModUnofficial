package com.myname.legacyloader.bridge.client.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;

public interface LegacyResource {
    InputStream getInputStream() throws IOException;

    InputStream func_110527_b() throws IOException;

    ResourceLocation getResourceLocation();

    ResourceLocation func_177241_a();

    record Wrapped(Resource resource, ResourceLocation location) implements LegacyResource {
        @Override
        public InputStream getInputStream() throws IOException {
            return resource.open();
        }

        @Override
        public InputStream func_110527_b() throws IOException {
            return getInputStream();
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return location;
        }

        @Override
        public ResourceLocation func_177241_a() {
            return location;
        }
    }
}
