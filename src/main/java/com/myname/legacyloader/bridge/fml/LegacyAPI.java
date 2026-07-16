package com.myname.legacyloader.bridge.fml;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LegacyAPI { String owner() default ""; String provides() default ""; String apiVersion() default ""; }
