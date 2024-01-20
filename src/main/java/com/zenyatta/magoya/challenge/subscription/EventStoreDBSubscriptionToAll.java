package com.zenyatta.magoya.challenge.subscription;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.Position;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.Subscription;
import com.eventstore.dbclient.SubscriptionListener;
import com.zenyatta.magoya.challenge.event.EventBus;
import com.zenyatta.magoya.challenge.event.util.EventEnvelope;
import com.zenyatta.magoya.challenge.event.util.EventTypeMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
public class EventStoreDBSubscriptionToAll {
    private final EventStoreDBClient eventStoreClient;
    private final SubscriptionCheckpointRepository checkpointRepository;
    private final EventBus eventBus;
    private EventStoreDBSubscriptionToAllOptions subscriptionOptions;
    private Subscription subscription;
    private boolean isRunning;

    private final RetryTemplate retryTemplate = RetryTemplate.builder()
            .infiniteRetry()
            .exponentialBackoff(100, 2, 5000)
            .build();

    private final SubscriptionListener listener = new SubscriptionListener() {
        @Override
        public void onEvent(final Subscription subscription, final ResolvedEvent event) {
            handleEvent(event);
        }

        @Override
        public void onError(final Subscription subscription, final Throwable throwable) {
            log.error("Subscription was dropped", throwable);

            throw new RuntimeException(throwable);
        }
    };

    public EventStoreDBSubscriptionToAll(
            final EventStoreDBClient eventStoreClient,
            final SubscriptionCheckpointRepository checkpointRepository,
            final EventBus eventBus) {
        this.eventStoreClient = eventStoreClient;
        this.checkpointRepository = checkpointRepository;
        this.eventBus = eventBus;
    }

    public void subscribeToAll() {
        subscribeToAll(EventStoreDBSubscriptionToAllOptions.getDefault());
    }

    void subscribeToAll(final EventStoreDBSubscriptionToAllOptions subscriptionOptions) {
        this.subscriptionOptions = subscriptionOptions;

        try {
            retryTemplate.execute(context -> {
                final Optional<Long> checkpoint = checkpointRepository.load(subscriptionOptions.subscriptionId());

                if (checkpoint.isPresent()) {
                    subscriptionOptions.subscribeToAllOptions()
                            .fromPosition(new Position(checkpoint.get(), checkpoint.get()));
                } else {
                    subscriptionOptions.subscribeToAllOptions()
                            .fromStart();
                }

                if (log.isInfoEnabled()) {
                    log.info("Subscribing to all '%s'".formatted(subscriptionOptions.subscriptionId()));
                }

                subscription = eventStoreClient.subscribeToAll(
                        listener,
                        subscriptionOptions.subscribeToAllOptions()).get();
                return null;
            });
        } catch (final Exception exception) {
            log.error("Error while starting subscription", exception);
            throw new RuntimeException(exception);
        }
    }

    public void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        subscription.stop();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    
    /** This method handles an event that has been resolved and deserializes it
     * to push into the internal event bus.
     * <p/>
     * Sometimes, the stream can be empty if we're sharing the database between multiple modules
     * and there are no filters in place. We may obtain events from other modules or a replica could've
     * processed an event before we did. If that's the case, we can ignore it.
     * <p/>
     * In a real world event, we should check if it should be ignored or not. For the sake of simplicity
     * in this challenge, I will not.

     * @param resolvedEvent
     */
    private void handleEvent(final ResolvedEvent resolvedEvent) {
        if (isEventWithEmptyData(resolvedEvent) || isCheckpointEvent(resolvedEvent)) {
            return;
        }

        final Optional<Class> eventClass = EventTypeMapper.toClass(resolvedEvent.getEvent().getEventType());
        final Optional<Object> streamEvent = eventClass.flatMap(c -> EventEnvelope.of(c, resolvedEvent));

        if (streamEvent.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("Couldn't deserialize event with id: %s".formatted(resolvedEvent.getEvent().getEventId()));
            }

            return;
        }

        eventBus.publish((EventEnvelope<?>) streamEvent.get());
        checkpointRepository.store(
                this.subscriptionOptions.subscriptionId(),
                resolvedEvent.getEvent().getPosition().getCommitUnsigned());
    }

    private boolean isEventWithEmptyData(final ResolvedEvent resolvedEvent) {
        if (resolvedEvent.getEvent().getEventData().length != 0) {
            return false;
        }

        log.info("Event without data received");
        return true;
    }

    private boolean isCheckpointEvent(final ResolvedEvent resolvedEvent) {
        if (!resolvedEvent.getEvent().getEventType().equals(EventTypeMapper.toName(CheckpointStored.class))) {
            return false;
        }

        log.info("Checkpoint event - ignoring");
        return true;
    }
}
