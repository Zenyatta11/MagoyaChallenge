package com.zenyatta.magoya.challenge.subscription;

import java.time.OffsetDateTime;

record CheckpointStored(
        String subscriptionId,
        long position,
        OffsetDateTime checkpointedAt
) {}
