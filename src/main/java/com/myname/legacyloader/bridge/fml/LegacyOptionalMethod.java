package com.myname.legacyloader.bridge.fml;
import java.lang.annotation.*;
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface LegacyOptionalMethod { String modid() default ""; }
