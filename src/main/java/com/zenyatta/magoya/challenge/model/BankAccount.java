package com.zenyatta.magoya.challenge.model;

import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent.Opened;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent.TransactionExecuted;
import java.time.OffsetDateTime;
import java.util.UUID;

public sealed interface BankAccount {
    record Initial() implements BankAccount {
    }

    record Open(
            UUID id,
            UUID clientId,
            double balance) implements BankAccount {
    }

    record Paused(
            UUID id,
            UUID clientId,
            double balance,
            OffsetDateTime pausedAt) implements BankAccount {
    }

    record Closed(
            UUID id,
            UUID clientId,
            double balance,
            OffsetDateTime closedAt) implements BankAccount {
    }

    default boolean isClosed() {
        return this instanceof Paused || this instanceof Closed;
    }

    static BankAccount when(final BankAccount current, final BankAccountEvent triggeredEvent) {
        return switch (triggeredEvent) {
            case Opened event -> {
                if (!(current instanceof Initial)) {
                    yield current;
                }
    
                yield new Open(
                    event.bankAccountId(),
                    event.clientId(),
                    0
                );
            }
    
            case TransactionExecuted event -> {
                if (!(current instanceof final Open account)) {
                    yield current;
                }

                yield new Open(
                    account.id(),
                    account.clientId(),
                    account.balance()
                );
            }
    
            case null -> throw new IllegalArgumentException("Event cannot be null!");
        };
    }
    

    static BankAccount empty() {
        return new Initial();
    }

    static String mapToStreamId(final UUID accountId) {
        return "BankAccount-%s".formatted(accountId);
    }
}