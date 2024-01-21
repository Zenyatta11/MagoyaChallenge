package com.zenyatta.magoya.challenge.event.domain;

import com.zenyatta.magoya.challenge.command.BankAccountCommand;
import com.zenyatta.magoya.challenge.event.EventStoreHandler;
import com.zenyatta.magoya.challenge.model.TransactionType;
import com.zenyatta.magoya.challenge.projection.JPAProjection;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * This interface specifies the events available in the
 * Event Store. They must be triggered by a corresponding
 * command to be processed by the projection entity.
 * 
 * @see EventStoreHandler
 * @see BankAccountCommand
 * @see JPAProjection           Generic Projection. For specific type, view BankAccountDetailsProjection
 */
public sealed interface BankAccountEvent {

    /**
     * Account opened event. This event is called when an account
     * is to be opened.

     * @param bankAccountId     Bank account ID
     * @param clientID          Client ID
     */
    record Opened(
            UUID bankAccountId,
            UUID clientId
    ) implements BankAccountEvent {
    }

    /**
     * TransactionExecuted event. This event is called when an
     * account movement is to be made.

     * @param bankAccountID The ID of the bank account (must not be a client ID)
     * @param type          The type of transaction
     * @param amount        The amount to work with
     * @param executedAt    Auditing date-time
     * @see TransactionType
     */
    record TransactionExecuted(
            UUID bankAccountId,
            TransactionType type,
            double amount,
            OffsetDateTime executedAt
    ) implements BankAccountEvent {
    }

}
