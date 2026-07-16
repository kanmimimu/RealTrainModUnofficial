package com.myname.legacyloader.bridge.crash;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class LegacyCrashReport {
    private final String description;
    private final Throwable cause;
    private final LegacyCrashReportCategory systemDetails = new LegacyCrashReportCategory(this, "System Details");
    private final List<LegacyCrashReportCategory> sections = new ArrayList<>();
    private File crashReportFile;

    public LegacyCrashReport(String description, Throwable cause) {
        this.description = description == null ? "" : description;
        this.cause = cause == null ? new RuntimeException(this.description) : cause;
    }

    public String getDescription() {
        return this.description;
    }

    public Throwable getCrashCause() {
        return this.cause;
    }

    public LegacyCrashReportCategory getCategory() {
        return this.systemDetails;
    }

    public LegacyCrashReportCategory makeCategory(String name) {
        LegacyCrashReportCategory category = new LegacyCrashReportCategory(this, name);
        this.sections.add(category);
        return category;
    }

    public LegacyCrashReportCategory makeCategoryDepth(String name, int depth) {
        LegacyCrashReportCategory category = makeCategory(name);
        category.getPrunedStackTrace(depth);
        return category;
    }

    public String getCompleteReport() {
        StringWriter writer = new StringWriter();
        PrintWriter print = new PrintWriter(writer);
        print.println("---- Minecraft Crash Report ----");
        print.println("// LegacyLoader 1.7.10 compatibility crash report");
        print.println();
        print.println("Description: " + this.description);
        print.println();
        this.cause.printStackTrace(print);
        print.println();
        this.systemDetails.appendToStringBuilder(new StringBuilder()).toString();
        for (LegacyCrashReportCategory section : this.sections) {
            print.println(section);
        }
        return writer.toString();
    }

    public boolean saveToFile(File file) {
        if (file == null) {
            return false;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(getCompleteReport());
            this.crashReportFile = file;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public File getFile() {
        return this.crashReportFile;
    }
}
