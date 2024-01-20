package com.zenyatta.magoya.challenge.event;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventDataBuilder;
import com.eventstore.dbclient.ResolvedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zenyatta.magoya.challenge.event.util.EventTypeMapper;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EventSerializer {

    private EventSerializer() {}

    public static final ObjectMapper mapper = new JsonMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static EventData serialize(final Object event) {
        try {
            return EventDataBuilder.json(
                    UUID.randomUUID(),
                    EventTypeMapper.toName(event.getClass()),
                    mapper.writeValueAsBytes(event)).build();
        } catch (final JsonProcessingException exception) {
            if (log.isErrorEnabled()) {
                log.error("Unable to serialize event", exception);
            }
            
            throw new RuntimeException(exception);
        }
    }

    public static <EventT> Optional<EventT> deserialize(final ResolvedEvent resolvedEvent) {
        final Optional<Class> eventClass = EventTypeMapper.toClass(resolvedEvent.getEvent().getEventType());

        if (eventClass.isEmpty()) {
            return Optional.empty();
        }

        return deserialize(eventClass.get(), resolvedEvent);
    }

    public static <EventT> Optional<EventT> deserialize(final Class<EventT> eventClass, final ResolvedEvent resolvedEvent) {
        try {

            final EventT result = mapper.readValue(resolvedEvent.getEvent().getEventData(), eventClass);

            if (result == null) {
                return Optional.empty();
            }

            return Optional.of(result);
        } catch (final IOException exception) {
            if (log.isWarnEnabled()) {
                log.warn("Error deserializing event %s, returning empty event.".formatted(
                        resolvedEvent.getEvent().getEventType()
                    ), exception);
            }

            return Optional.empty();
        }
    }
}
