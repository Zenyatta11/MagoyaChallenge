package com.zenyatta.magoya.challenge.event;

import com.eventstore.dbclient.AppendToStreamOptions;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ExpectedRevision;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.StreamNotFoundException;
import com.eventstore.dbclient.WriteResult;
import com.zenyatta.magoya.challenge.utils.http.ETag;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventStoreHandler<T, C, E> {
    private final EventStoreDBClient eventStore;
    private final BiFunction<T, E, T> when;
    private final Function<UUID, String> mapToStreamId;
    private final Supplier<T> getDefault;
    private final BiFunction<C, T, E> handle;

    public EventStoreHandler(
            final EventStoreDBClient eventStore,
            final BiFunction<T, E, T> when,
            final BiFunction<C, T, E> handle,
            final Function<UUID, String> mapToStreamId,
            final Supplier<T> getDefault) {

        this.eventStore = eventStore;
        this.when = when;
        this.handle = handle;
        this.mapToStreamId = mapToStreamId;
        this.getDefault = getDefault;
    }

    
    /** This is a generic event handler for the subscribed store.
     * It is in charge of executing the command with a specified version in the specified entity (by ID).
     * Returns the new version number of the entity if it succeeds.

     * @param id
     * @param command
     * @param expectedRevision
     * @return ETag
     */
    public ETag handle(
            final UUID id,
            final C command,
            final ExpectedRevision expectedRevision) {
        final String streamId = mapToStreamId.apply(id);
        final Optional<T> entity = get(id);

        if (entity.isEmpty() && !expectedRevision.equals(ExpectedRevision.noStream())) {
            throw new EntityNotFoundException();
        }

        final E event = handle.apply(command, entity.orElse(getDefault.get()));

        try {
            final WriteResult result = eventStore.appendToStream(
                    streamId,
                    AppendToStreamOptions.get().expectedRevision(expectedRevision),
                    EventSerializer.serialize(event)).get();

            return toETag(result.getNextExpectedRevision());
        } catch (
            InterruptedException | 
            ExecutionException | 
            NoSuchFieldException | 
            IllegalAccessException exception
        ) {
            throw new RuntimeException(exception);
        }
    }


    
    /** Gets the entity by UUID.

    * @param id
     * @return Optional
     */
    private Optional<T> get(final UUID id) {
        final String streamId = mapToStreamId.apply(id);

        final Optional<List<E>> events = getEvents(streamId);

        if (events.isEmpty()) {
            return Optional.empty();
        }

        T current = getDefault.get();

        for (final E event : events.get()) {
            current = when.apply(current, event);
        }

        return Optional.of(current);
    }

    
    /** Gets the events from a specified stream (by ID).
     * If the stream does not exist or cannot be connected to, it returns an empty list.
     * </p>
     * @param streamId
     * @return Optional
     */
    private Optional<List<E>> getEvents(final String streamId) {
        ReadResult result;
        try {
            result = eventStore.readStream(streamId, ReadStreamOptions.get()).get();
        } catch (final StreamNotFoundException | ExecutionException exception) {
            log.warn("Stream not found, returning empty event", exception);
            return Optional.empty();
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }

        final List<E> events = result.getEvents().stream()
                .map(EventSerializer::<E>deserialize)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return Optional.of(events);
    }

    
    /** This is a shameful but necessary way to be able to elegantly serialize the version field.
     * As of Java's ESDB v4 and onwards, the field is not accessible from outside.
     * A workaround through this would be too complex

     * @param nextExpectedRevision
     * @return ETag
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private ETag toETag(final ExpectedRevision nextExpectedRevision) throws NoSuchFieldException, IllegalAccessException {
        final Field field = nextExpectedRevision.getClass().getDeclaredField("version");
        field.setAccessible(true);

        return ETag.weak(field.get(nextExpectedRevision));
    }
}
