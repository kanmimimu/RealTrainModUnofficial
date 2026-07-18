package com.myname.legacyloader.bridge.command;

/**
 * 1.7.10縺ｮ CommandException 莠呈鋤繧ｯ繝ｩ繧ｹ
 */
public class LegacyCommandException extends Exception {

    private final Object[] args;

    public LegacyCommandException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public Object[] getErrorObjects() {
        return args;
    }

    /**
     * 1.7.10 SRG: func_74844_a
     */
    public Object[] func_74844_a() {
        return getErrorObjects();
    }
}