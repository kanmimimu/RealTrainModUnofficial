package com.myname.legacyloader.bridge.fml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 1.7.10縺ｮ @Mod.Metadata 莠呈鋤繧｢繝弱ユ繝ｼ繧ｷ繝ｧ繝ｳ
 * 繝輔ぅ繝ｼ繝ｫ繝峨↓ModMetadata繧呈ｳｨ蜈･縺吶ｋ縺溘ａ縺ｫ菴ｿ逕ｨ
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LegacyModMetadataAnnotation {
    /**
     * MOD ID繧呈欠螳夲ｼ育ｩｺ縺ｮ蝣ｴ蜷医・隕ｪMOD縺ｮID繧剃ｽｿ逕ｨ・・
     */
    String value() default "";
}