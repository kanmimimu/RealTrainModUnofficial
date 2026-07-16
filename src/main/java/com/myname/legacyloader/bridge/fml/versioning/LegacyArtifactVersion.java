package com.myname.legacyloader.bridge.fml.versioning;

public interface LegacyArtifactVersion extends Comparable<LegacyArtifactVersion> {
    String getLabel();

    String getVersionString();

    boolean containsVersion(LegacyArtifactVersion version);

    String getRangeString();
}
