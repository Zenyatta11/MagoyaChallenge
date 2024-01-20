package com.zenyatta.magoya.challenge.event.util;

import com.eventstore.dbclient.ResolvedEvent;
import com.zenyatta.magoya.challenge.event.EventSerializer;
import java.util.Optional;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

public record EventEnvelope<EventT>(
        EventT data,
        EventMetadata metadata
) implements ResolvableTypeProvider {

    public static <EventT> Optional<EventEnvelope<EventT>> of(final Class<EventT> type, final ResolvedEvent resolvedEvent) {
        if (type == null) {
            return Optional.empty();
        }

        final Optional<EventT> eventData = EventSerializer.deserialize(type, resolvedEvent);

        if (eventData.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                new EventEnvelope<>(
                        eventData.get(),
                        new EventMetadata(
                                resolvedEvent.getEvent().getEventId().toString(),
                                resolvedEvent.getEvent().getRevision(),
                                resolvedEvent.getEvent().getPosition().getCommitUnsigned(),
                                resolvedEvent.getEvent().getEventType())));
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                getClass(), ResolvableType.forInstance(data));
    }
}
