package com.myname.legacyloader.bridge.fml;

import java.io.PrintStream;

public class LegacyEnhancedRuntimeException extends RuntimeException {
    public LegacyEnhancedRuntimeException() {
    }

    public LegacyEnhancedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public void printStackTrace(WrappedPrintStream stream) {
        super.printStackTrace(stream.stream);
    }

    public static class WrappedPrintStream {
        private final PrintStream stream;

        public WrappedPrintStream(PrintStream stream) {
            this.stream = stream;
        }

        public void println(String line) {
            this.stream.println(line);
        }
    }
}
