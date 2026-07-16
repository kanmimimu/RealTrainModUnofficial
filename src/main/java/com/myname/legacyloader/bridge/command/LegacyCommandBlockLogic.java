package com.myname.legacyloader.bridge.command;

public class LegacyCommandBlockLogic {
    private String command = "";
    private String lastOutput = "";
    private int successCount;

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command == null ? "" : command;
    }

    public String func_145753_i() {
        return getCommand();
    }

    public void func_145752_a(String command) {
        setCommand(command);
    }

    public int func_145760_g() {
        return this.successCount;
    }

    public void func_145750_b(Object output) {
        this.lastOutput = String.valueOf(output);
    }

    public String func_145749_h() {
        return this.lastOutput;
    }
}
