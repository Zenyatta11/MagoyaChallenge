package com.zenyatta.magoya.challenge.config;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.zenyatta.magoya.challenge.event.EventBus;
import com.zenyatta.magoya.challenge.service.EventSubscriptionBackgroundWorker;
import com.zenyatta.magoya.challenge.subscription.EventStoreDBSubscriptionCheckpointRepository;
import com.zenyatta.magoya.challenge.subscription.SubscriptionCheckpointRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
class EventStoreDBConfig {

    @Bean
    @Scope("singleton")
    EventStoreDBClient eventStoreDBClient(@Value("${esdb.connectionstring}") final String connectionString) {
        try {
            final EventStoreDBClientSettings settings = EventStoreDBConnectionString.parse(connectionString);

            return EventStoreDBClient.create(settings);
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Bean
    EventSubscriptionBackgroundWorker eventStoreDBSubscriptionBackgroundWorker(
            final EventStoreDBClient eventStore,
            final SubscriptionCheckpointRepository subscriptionCheckpointRepository,
            final EventBus eventBus) {
        return new EventSubscriptionBackgroundWorker(eventStore, subscriptionCheckpointRepository, eventBus);
    }

    @Bean
    SubscriptionCheckpointRepository subscriptionCheckpointRepository(final EventStoreDBClient eventStore) {
        return new EventStoreDBSubscriptionCheckpointRepository(eventStore);
    }
    
}
