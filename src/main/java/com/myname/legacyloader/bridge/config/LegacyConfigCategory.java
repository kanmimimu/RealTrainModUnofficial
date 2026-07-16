package com.myname.legacyloader.bridge.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class LegacyConfigCategory {
    public final LegacyConfigCategory parent;
    private final String name;
    private final Map<String, LegacyProperty> values = new LinkedHashMap<>();
    private final List<LegacyConfigCategory> children = new ArrayList<>();
    private String comment;
    private String languageKey;
    private boolean showInGui = true;
    private boolean requiresWorldRestart;
    private boolean requiresMcRestart;
    private List<String> propertyOrder;

    public LegacyConfigCategory(String name) {
        this(name, null);
    }

    public LegacyConfigCategory(String name, LegacyConfigCategory parent) {
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public String getName() {
        return name;
    }

    public LegacyProperty get(String key) {
        return values.get(key);
    }

    public LegacyProperty put(String key, LegacyProperty property) {
        return values.put(key, property);
    }

    public LegacyProperty remove(Object key) {
        return values.remove(key);
    }

    public void putAll(Map<? extends String, ? extends LegacyProperty> map) {
        values.putAll(map);
    }

    public void clear() {
        values.clear();
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return values.containsValue(value);
    }

    public Collection<LegacyProperty> values() {
        return values.values();
    }

    public java.util.Set<String> keySet() {
        return values.keySet();
    }

    public java.util.Set<Map.Entry<String, LegacyProperty>> entrySet() {
        return values.entrySet();
    }

    public String getQualifiedName() {
        return parent == null ? name : parent.getQualifiedName() + "." + name;
    }

    public static String getQualifiedName(String name, LegacyConfigCategory parent) {
        return parent == null ? name : parent.getQualifiedName() + "." + name;
    }

    public LegacyConfigCategory getFirstParent() {
        return parent == null ? this : parent.getFirstParent();
    }

    public boolean isChild() {
        return parent != null;
    }

    public Map<String, LegacyProperty> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public List<LegacyProperty> getOrderedValues() {
        if (propertyOrder == null) {
            return List.copyOf(values.values());
        }

        List<LegacyProperty> ordered = new ArrayList<>();
        for (String key : propertyOrder) {
            LegacyProperty property = values.get(key);
            if (property != null) {
                ordered.add(property);
            }
        }
        return List.copyOf(ordered);
    }

    public LegacyConfigCategory setLanguageKey(String languageKey) {
        this.languageKey = languageKey;
        return this;
    }

    public String getLanguagekey() {
        return languageKey == null ? getQualifiedName() : languageKey;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public LegacyConfigCategory setRequiresWorldRestart(boolean requiresWorldRestart) {
        this.requiresWorldRestart = requiresWorldRestart;
        return this;
    }

    public boolean requiresWorldRestart() {
        return requiresWorldRestart;
    }

    public LegacyConfigCategory setRequiresMcRestart(boolean requiresMcRestart) {
        this.requiresMcRestart = requiresMcRestart;
        this.requiresWorldRestart = requiresMcRestart;
        return this;
    }

    public boolean requiresMcRestart() {
        return requiresMcRestart;
    }

    public LegacyConfigCategory setShowInGui(boolean showInGui) {
        this.showInGui = showInGui;
        return this;
    }

    public boolean showInGui() {
        return showInGui;
    }

    public Collection<LegacyConfigCategory> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public LegacyConfigCategory setPropertyOrder(List<String> propertyOrder) {
        this.propertyOrder = propertyOrder == null ? null : new ArrayList<>(propertyOrder);
        if (this.propertyOrder != null) {
            for (String key : values.keySet()) {
                if (!this.propertyOrder.contains(key)) {
                    this.propertyOrder.add(key);
                }
            }
        }
        return this;
    }

    public List<String> getPropertyOrder() {
        if (propertyOrder != null) {
            return List.copyOf(propertyOrder);
        }
        return List.copyOf(values.keySet());
    }
}
