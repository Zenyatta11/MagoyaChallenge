package com.zenyatta.magoya.challenge.subscription;

import com.eventstore.dbclient.AppendToStreamOptions;
import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ExpectedRevision;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.StreamMetadata;
import com.eventstore.dbclient.StreamNotFoundException;
import com.eventstore.dbclient.WrongExpectedVersionException;
import com.zenyatta.magoya.challenge.event.EventSerializer;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EventStoreDBSubscriptionCheckpointRepository implements SubscriptionCheckpointRepository {
    private static String getCheckpointStreamName(final String subscriptionId) {
        return "checkpoint-%s".formatted(subscriptionId);
    }

    private final EventStoreDBClient eventStore;

    public EventStoreDBSubscriptionCheckpointRepository(
            final EventStoreDBClient eventStore
    ) {
        this.eventStore = eventStore;
    }

    /** Loads the latest checkpoint in a specified subscription store.

    * @param subscriptionId
     * @return Optional
     * @throws RuntimeException
     */
    public Optional<Long> load(final String subscriptionId) {
        final String streamName = getCheckpointStreamName(subscriptionId);

        final ReadStreamOptions readOptions = ReadStreamOptions.get()
                .backwards()
                .fromEnd();

        try {
            return eventStore.readStream(streamName, readOptions)
                    .get()
                    .getEvents()
                    .stream()
                    .map(
                        event -> EventSerializer.<CheckpointStored>deserialize(event)
                            .map(checkpoint -> checkpoint.position())
                    ).findFirst()
                    .orElse(Optional.empty());

        } catch (final StreamNotFoundException | ExecutionException exception) {
            log.warn("Stream was not found, returning an empty version", exception);
            return Optional.empty();
        } catch (InterruptedException exception) {
            log.error("Failed to load checkpoint", exception);
            throw new RuntimeException(exception);
        }
    }

    /** Stores a new checkpoint in the specified subscription store in a specific position.
     * If the position is not the last one, it will ignore it.

     * @param subscriptionId
     * @param position
     * @throws RuntimeException
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void store(final String subscriptionId, final long position) {
        final EventData event = EventSerializer.serialize(
                new CheckpointStored(subscriptionId, position, OffsetDateTime.now())
            );

        final String streamName = getCheckpointStreamName(subscriptionId);

        try {
            eventStore.appendToStream(
                    streamName,
                    AppendToStreamOptions.get().expectedRevision(ExpectedRevision.streamExists()),
                    event).get();
        } catch (final WrongExpectedVersionException exception) {
            final StreamMetadata keepOnlyLastEvent = new StreamMetadata();
            keepOnlyLastEvent.setMaxCount(1);

            try {
                eventStore.setStreamMetadata(
                        streamName,
                        AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream()),
                        keepOnlyLastEvent).get();

                eventStore.appendToStream(
                        streamName,
                        AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream()),
                        event).get();
            } catch (InterruptedException | ExecutionException subexception) {
                throw new RuntimeException(subexception);
            }
        } catch (InterruptedException | ExecutionException exception) {
            throw new RuntimeException(exception);
        }
    }
}
