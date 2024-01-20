package com.zenyatta.magoya.challenge.subscription;

import com.eventstore.dbclient.SubscribeToAllOptions;
import com.eventstore.dbclient.SubscriptionFilter;

record EventStoreDBSubscriptionToAllOptions(
        String subscriptionId,
        boolean ignoreDeserializationErrors,
        SubscribeToAllOptions subscribeToAllOptions) {
    static EventStoreDBSubscriptionToAllOptions getDefault() {
        final SubscriptionFilter filterOutSystemEvents = SubscriptionFilter.newBuilder()
                .withEventTypeRegularExpression("^[^\\$].*")
                .build();

        final SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .fromStart()
                .filter(filterOutSystemEvents);

        return new EventStoreDBSubscriptionToAllOptions("default", true, options);
    }
}
