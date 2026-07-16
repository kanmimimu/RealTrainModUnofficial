package com.myname.legacyloader.bridge.event;

import com.google.common.collect.ImmutableList;
import com.myname.legacyloader.bridge.fml.LegacyEvent;
import net.minecraft.nbt.CompoundTag;

public final class LegacyFMLInterModComms {
    private LegacyFMLInterModComms() {
    }

    public static boolean sendMessage(String modId, String key, String value) {
        return true;
    }

    public static boolean sendMessage(String modId, String key, CompoundTag value) {
        return true;
    }

    public static boolean sendRuntimeMessage(String sourceModId, String modId, String key, CompoundTag value) {
        return true;
    }

    public static class IMCEvent extends LegacyEvent {
        public ImmutableList<IMCMessage> getMessages() {
            return ImmutableList.of();
        }
    }

    public static class IMCMessage {
        public String key = "";
        private String sender = "";
        private String stringValue = "";
        private CompoundTag nbtValue;

        public boolean isStringMessage() {
            return stringValue != null;
        }

        public String getStringValue() {
            return stringValue == null ? "" : stringValue;
        }

        public boolean isNBTMessage() {
            return nbtValue != null;
        }

        public CompoundTag getNBTValue() {
            return nbtValue == null ? new CompoundTag() : nbtValue;
        }

        public String getSender() {
            return sender == null ? "" : sender;
        }
    }
}
