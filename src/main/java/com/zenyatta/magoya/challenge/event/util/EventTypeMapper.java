package com.zenyatta.magoya.challenge.event.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EventTypeMapper {
    private static final EventTypeMapper Instance = new EventTypeMapper();

    private final Map<String, Optional<Class>> typeMap = new HashMap<>();
    private final Map<Class, String> typeNameMap = new HashMap<>();

    public static String toName(final Class eventType) {
        return Instance.typeNameMap.computeIfAbsent(
                eventType,
                c -> c.getTypeName().replace("$", "__"));
    }

    public static Optional<Class> toClass(final String eventTypeName) {
        return Instance.typeMap.computeIfAbsent(
                eventTypeName,
                c -> {
                    try {
                        return Optional.of(Class.forName(eventTypeName.replace("__", "$")));
                    } catch (final ClassNotFoundException e) {
                        return Optional.empty();
                    }
                });
    }
}
