package com.myname.legacyloader.bridge.event;

import com.google.common.collect.ListMultimap;

import java.util.Collections;
import java.util.List;

public class LegacyMissingMappingsEvent {
    public LegacyMissingMappingsEvent() {}

    public LegacyMissingMappingsEvent(ListMultimap<?, MissingMapping> mappings) {}

    public List<MissingMapping> get() {
        return Collections.emptyList();
    }

    public enum Action {
        DEFAULT,
        IGNORE,
        WARN,
        FAIL,
        REMAP
    }

    public static class MissingMapping {
        public final String name;
        public final int type;
        private Action action = Action.DEFAULT;
        private Object target;

        public MissingMapping(String name, int type) {
            this.name = name;
            this.type = type;
        }

        public void remap(Object target) {
            this.target = target;
            this.action = Action.REMAP;
        }

        public Action getAction() {
            return action;
        }

        public Object getTarget() {
            return target;
        }
    }
}
