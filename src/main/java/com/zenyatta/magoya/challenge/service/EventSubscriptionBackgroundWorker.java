package com.zenyatta.magoya.challenge.service;

import com.eventstore.dbclient.EventStoreDBClient;
import com.zenyatta.magoya.challenge.event.EventBus;
import com.zenyatta.magoya.challenge.subscription.EventStoreDBSubscriptionToAll;
import com.zenyatta.magoya.challenge.subscription.SubscriptionCheckpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

@Slf4j
public class EventSubscriptionBackgroundWorker implements SmartLifecycle {

    private final SubscriptionCheckpointRepository subscriptionCheckpointRepository;
    private final EventStoreDBClient eventStore;
    private final EventBus eventBus;

    private EventStoreDBSubscriptionToAll subscription;

    public EventSubscriptionBackgroundWorker(
            final EventStoreDBClient eventStore,
            final SubscriptionCheckpointRepository subscriptionCheckpointRepository,
            final EventBus eventBus) {
        this.eventStore = eventStore;
        this.subscriptionCheckpointRepository = subscriptionCheckpointRepository;
        this.eventBus = eventBus;
    }

    @Override
    public void start() {
        try {
            subscription = new EventStoreDBSubscriptionToAll(
                    eventStore,
                    subscriptionCheckpointRepository,
                    eventBus);

            subscription.subscribeToAll();
        } catch (final Throwable exception) {
            log.error("Failed to start Subscription to All", exception);
        }
    }

    @Override
    public void stop() {
        stop(
                () -> {
                });
    }

    @Override
    public void stop(final Runnable callback) {
        if (!isRunning()) {
            return;
        }

        subscription.stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return subscription != null && subscription.isRunning();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

}
