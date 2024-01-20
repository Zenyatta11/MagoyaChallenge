package com.zenyatta.magoya.challenge.command;

import com.zenyatta.magoya.challenge.model.TransactionType;
import java.util.UUID;

public sealed interface BankAccountCommand {

    record Open(
            UUID bankAccountId,
            UUID clientId) implements BankAccountCommand {
    }

    record ExecuteTransaction(
            UUID bankAccountId,
            TransactionType type,
            double amount) implements BankAccountCommand {
    }

}
