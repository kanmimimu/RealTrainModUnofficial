package com.myname.legacyloader.bridge.fml;

import java.util.concurrent.Callable;

public interface LegacyICrashCallable extends Callable<String> {
    String getLabel();
}
