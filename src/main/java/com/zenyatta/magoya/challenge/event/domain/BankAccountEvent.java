package com.zenyatta.magoya.challenge.event.domain;

import com.zenyatta.magoya.challenge.model.TransactionType;
import java.time.OffsetDateTime;
import java.util.UUID;

public sealed interface BankAccountEvent {

    record Opened(
            UUID bankAccountId,
            UUID clientId
    ) implements BankAccountEvent {
    }

    record TransactionExecuted(
            UUID bankAccountId,
            TransactionType type,
            double amount,
            OffsetDateTime executedAt
    ) implements BankAccountEvent {
    }

}
