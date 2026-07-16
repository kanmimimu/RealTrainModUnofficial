package com.myname.legacyloader.bridge.fml;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BridgeEventBus implements IEventBus {
    private final List<Consumer<? extends Event>> listeners = new ArrayList<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void postFakeEvent(Event event) {
        for (Consumer handler : listeners) {
            try {
                handler.accept(event);
            } catch (ClassCastException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void register(Object target) {
        if (target == null) return;
        for (Method method : target.getClass().getMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class)) continue;
            if (method.getParameterCount() != 1) continue;
            Class<?> eventType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventType)) continue;
            method.setAccessible(true);
            listeners.add(event -> {
                if (!eventType.isInstance(event)) return;
                try {
                    method.invoke(target, event);
                } catch (Throwable t) {
                    Throwable cause = t instanceof java.lang.reflect.InvocationTargetException ite
                            ? ite.getCause()
                            : t;
                    if (cause != null) cause.printStackTrace();
                }
            });
        }
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(boolean receiveCanceled, Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public <T extends Event> void addListener(boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer) {
        listeners.add(consumer);
    }

    @Override
    public void unregister(Object object) {
    }

    @Override
    public <T extends Event> T post(T event) {
        postFakeEvent(event);
        return event;
    }

    public boolean post(LegacyEvent event) {
        postFakeEvent(event);
        return event != null && event.isCanceled();
    }

    @Override
    public <T extends Event> T post(EventPriority phase, T event) {
        postFakeEvent(event);
        return event;
    }

    @Override
    public void start() {
    }
}
