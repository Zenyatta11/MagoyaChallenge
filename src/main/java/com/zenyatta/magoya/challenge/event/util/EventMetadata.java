package com.zenyatta.magoya.challenge.event.util;

public record EventMetadata(
        String eventId,
        long streamPosition,
        long logPosition,
        String eventType
) { }
