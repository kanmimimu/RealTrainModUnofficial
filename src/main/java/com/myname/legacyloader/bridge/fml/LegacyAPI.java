package com.myname.legacyloader.bridge.fml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LegacyAPI { String owner() default ""; String provides() default ""; String apiVersion() default ""; }
