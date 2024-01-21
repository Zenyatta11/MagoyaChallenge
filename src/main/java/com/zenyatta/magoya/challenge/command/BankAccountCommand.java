package com.zenyatta.magoya.challenge.command;

import com.zenyatta.magoya.challenge.model.TransactionType;
import com.zenyatta.magoya.challenge.event.EventStoreHandler;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent;
import java.util.UUID;

/**
 * This interface defines the available commands for the system.
 * They have to be in tune with the available events. Each command
 * will trigger its corresponding event in the store handler

 * @see EventStoreHandler
 * @see BankAccountEvent
 */
public sealed interface BankAccountCommand {

    /**
     * This command is for opening a bank account with a specified
     * account ID and client ID
     */
    record Open(
            UUID bankAccountId,
            UUID clientId) implements BankAccountCommand {
    }

    /**
     * This command is for executing transactions.

     * @param bankAccountId The ID of the bank account
     * @param type          The transaction type
     * @param amount        The amount to add/subtract
     * @see TransactionType
     */
    record ExecuteTransaction(
            UUID bankAccountId,
            TransactionType type,
            double amount) implements BankAccountCommand {
    }

}
