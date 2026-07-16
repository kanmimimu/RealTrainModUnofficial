package com.myname.legacyloader.bridge.fml;

public class LegacyEvent extends net.neoforged.bus.api.Event {
    public enum Result {
        DENY,
        DEFAULT,
        ALLOW
    }

    private boolean canceled;
    private Result result = Result.DEFAULT;

    public boolean isCancelable() {
        return true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean hasResult() {
        return true;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result != null ? result : Result.DEFAULT;
    }
}
