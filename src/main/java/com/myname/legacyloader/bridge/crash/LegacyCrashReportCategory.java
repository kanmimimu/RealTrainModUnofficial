package com.myname.legacyloader.bridge.crash;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class LegacyCrashReportCategory {
    private final LegacyCrashReport report;
    private final String name;
    private final List<String> entries = new ArrayList<>();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public LegacyCrashReportCategory(LegacyCrashReport report, String name) {
        this.report = report;
        this.name = name == null ? "" : name;
    }

    public static String func_85074_a(double x, double y, double z) {
        return String.format("%.2f,%.2f,%.2f - %s", x, y, z,
                getLocationInfo((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));
    }

    public static String getLocationInfo(int x, int y, int z) {
        return String.format("World: (%d,%d,%d), Chunk: (%d,%d in %d,%d)", x, y, z, x & 15, z & 15, x >> 4, z >> 4);
    }

    public void addCrashSection(String key, Object value) {
        this.entries.add(key + ": " + value);
    }

    public void addCrashSectionCallable(String key, Callable<?> callable) {
        try {
            addCrashSection(key, callable == null ? null : callable.call());
        } catch (Throwable throwable) {
            addCrashSectionThrowable(key, throwable);
        }
    }

    public void addCrashSectionThrowable(String key, Throwable throwable) {
        addCrashSection(key, throwable == null ? "~~NULL~~" : throwable.toString());
    }

    public int getPrunedStackTrace(int depth) {
        StackTraceElement[] current = Thread.currentThread().getStackTrace();
        int skip = Math.min(Math.max(depth, 0) + 2, current.length);
        this.stackTrace = new StackTraceElement[current.length - skip];
        System.arraycopy(current, skip, this.stackTrace, 0, this.stackTrace.length);
        return this.stackTrace.length;
    }

    public boolean firstTwoElementsOfStackTraceMatch(StackTraceElement first, StackTraceElement second) {
        return this.stackTrace.length >= 2 && this.stackTrace[0].equals(first) && this.stackTrace[1].equals(second);
    }

    public void trimStackTraceEntriesFromBottom(int count) {
        int nextLength = Math.max(0, this.stackTrace.length - Math.max(0, count));
        StackTraceElement[] next = new StackTraceElement[nextLength];
        System.arraycopy(this.stackTrace, 0, next, 0, nextLength);
        this.stackTrace = next;
    }

    public StringBuilder appendToStringBuilder(StringBuilder builder) {
        builder.append("-- ").append(this.name).append(" --\n");
        for (String entry : this.entries) {
            builder.append(entry).append('\n');
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendToStringBuilder(new StringBuilder()).toString();
    }
}
