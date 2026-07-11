package jp.ngt.mccompat;

import net.minecraft.util.Mth;

/**
 * パックスクリプト互換: 1.7.10/1.12 の net.minecraft.util.MathHelper。
 * プリリュードで `var MathHelper = Java.type(...)` としてバインドされる。
 */
@SuppressWarnings("unused")
public final class MathHelper {
    private MathHelper() {
    }

    public static float wrapAngleTo180_float(float value) {
        return Mth.wrapDegrees(value);
    }

    public static double wrapAngleTo180_double(double value) {
        return Mth.wrapDegrees(value);
    }

    /**
     * 1.12 SRG: wrapDegrees
     */
    public static float func_76142_g(float value) {
        return Mth.wrapDegrees(value);
    }

    public static double func_76138_g(double value) {
        return Mth.wrapDegrees(value);
    }

    public static float cos(float rad) {
        return Mth.cos(rad);
    }

    public static float sin(float rad) {
        return Mth.sin(rad);
    }

    public static float sqrt_float(float value) {
        return (float) Math.sqrt(value);
    }

    public static float sqrt_double(double value) {
        return (float) Math.sqrt(value);
    }

    public static int floor_double(double value) {
        return Mth.floor(value);
    }

    public static int floor_float(float value) {
        return Mth.floor(value);
    }

    public static int ceiling_double_int(double value) {
        return Mth.ceil(value);
    }

    public static float abs(float value) {
        return Math.abs(value);
    }

    public static int abs_int(int value) {
        return Math.abs(value);
    }

    public static float clamp_float(float value, float min, float max) {
        return Mth.clamp(value, min, max);
    }

    public static int clamp_int(int value, int min, int max) {
        return Mth.clamp(value, min, max);
    }

    public static double clamp_double(double value, double min, double max) {
        return Mth.clamp(value, min, max);
    }
}
