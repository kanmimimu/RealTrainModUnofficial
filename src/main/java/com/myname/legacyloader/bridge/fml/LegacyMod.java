package com.myname.legacyloader.bridge.fml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LegacyMod {
    String modid() default ""; String name() default ""; String version() default "";
    String dependencies() default ""; String acceptedMinecraftVersions() default "";
    String acceptableRemoteVersions() default ""; boolean useMetadata() default false;
    String certificateFingerprint() default ""; String modLanguage() default "java";
}
