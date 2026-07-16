package com.myname.legacyloader.bridge.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LegacyEntityList {
    public static final Map<String, Class<?>> stringToClassMapping = new HashMap<>();
    public static final Map<Class<?>, String> classToStringMapping = new HashMap<>();
    public static final Map<Integer, Class<?>> IDtoClassMapping = new HashMap<>();
    public static final Map<Class<?>, Integer> classToIDMapping = new HashMap<>();
    public static final Map<String, Integer> stringToIDMapping = new HashMap<>();
    public static final LinkedHashMap<Integer, EntityEggInfo> entityEggs = new LinkedHashMap<>();

    public static final Map<String, Class<?>> field_75625_b = stringToClassMapping;
    public static final Map<Class<?>, String> field_75626_c = classToStringMapping;
    public static final Map<Integer, Class<?>> field_75623_d = IDtoClassMapping;
    public static final Map<Class<?>, Integer> field_75624_e = classToIDMapping;
    public static final Map<String, Integer> field_75622_f = stringToIDMapping;
    public static final LinkedHashMap<Integer, EntityEggInfo> field_75627_a = entityEggs;

    public static void addMapping(Class<?> entityClass, String name, int id) {
        if (entityClass == null || name == null) return;
        stringToClassMapping.put(name, entityClass);
        classToStringMapping.put(entityClass, name);
        IDtoClassMapping.put(id, entityClass);
        classToIDMapping.put(entityClass, id);
        stringToIDMapping.put(name, id);
    }

    public static void func_75618_a(Class<?> entityClass, String name, int id) {
        addMapping(entityClass, name, id);
    }

    public static void addMapping(Class<?> entityClass, String name, int id, int primaryColor, int secondaryColor) {
        addMapping(entityClass, name, id);
        entityEggs.put(id, new EntityEggInfo(id, primaryColor, secondaryColor));
    }

    public static void func_75614_a(Class<?> entityClass, String name, int id, int primaryColor, int secondaryColor) {
        addMapping(entityClass, name, id, primaryColor, secondaryColor);
    }

    public static Entity createEntityByName(String name, Level level) {
        return create(stringToClassMapping.get(name), level);
    }

    public static Entity func_75620_a(String name, Level level) {
        return createEntityByName(name, level);
    }

    public static Entity createEntityByID(int id, Level level) {
        return create(IDtoClassMapping.get(id), level);
    }

    public static Entity func_75616_a(int id, Object level) {
        return level instanceof Level ? createEntityByID(id, (Level) level) : null;
    }

    public static Entity createEntityFromNBT(CompoundTag tag, Level level) {
        if (tag == null) return null;
        Entity entity = createEntityByName(tag.getString("id"), level);
        if (entity != null) {
            try {
                entity.load(tag);
            } catch (Throwable ignored) {
            }
        }
        return entity;
    }

    public static Entity func_75615_a(CompoundTag tag, Level level) {
        return createEntityFromNBT(tag, level);
    }

    public static String getEntityString(Entity entity) {
        return entity == null ? null : classToStringMapping.get(entity.getClass());
    }

    public static String func_75621_b(Object entity) {
        return entity instanceof Entity ? getEntityString((Entity) entity) : null;
    }

    public static int getEntityID(Entity entity) {
        if (entity == null) return 0;
        Integer id = classToIDMapping.get(entity.getClass());
        return id != null ? id : 0;
    }

    public static int func_75619_a(Object entity) {
        return entity instanceof Entity ? getEntityID((Entity) entity) : 0;
    }

    public static String getStringFromID(int id) {
        Class<?> entityClass = IDtoClassMapping.get(id);
        return entityClass != null ? classToStringMapping.get(entityClass) : null;
    }

    public static String func_75617_a(int id) {
        return getStringFromID(id);
    }

    private static Entity create(Class<?> entityClass, Level level) {
        if (entityClass == null || level == null) return null;
        try {
            Constructor<?> ctor = entityClass.getConstructor(Level.class);
            Object value = ctor.newInstance(level);
            return value instanceof Entity ? (Entity) value : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static class EntityEggInfo {
        public final int spawnedID;
        public final int primaryColor;
        public final int secondaryColor;
        public final int field_75613_a;
        public final int field_75611_b;
        public final int field_75612_c;

        public EntityEggInfo(int id, int primaryColor, int secondaryColor) {
            this.spawnedID = id;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
            this.field_75613_a = id;
            this.field_75611_b = primaryColor;
            this.field_75612_c = secondaryColor;
        }
    }
}
