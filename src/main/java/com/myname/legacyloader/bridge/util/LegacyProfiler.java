package com.myname.legacyloader.bridge.util;

/**
 * Stub for 1.7.10 Profiler - no-op since modern profiling works differently.
 */
public class LegacyProfiler {
    public void startSection(String name) {}
    public void endSection() {}
    public void endStartSection(String name) { endSection(); startSection(name); }

    // SRG aliases
    public void func_76320_a(String name) { startSection(name); }
    public void func_76319_b() { endSection(); }
    public void func_76318_c(String name) { endStartSection(name); }
}
