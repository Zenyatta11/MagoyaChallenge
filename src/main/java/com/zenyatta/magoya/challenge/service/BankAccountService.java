package com.zenyatta.magoya.challenge.service;

import com.zenyatta.magoya.challenge.command.BankAccountCommand;
import com.zenyatta.magoya.challenge.command.BankAccountCommand.ExecuteTransaction;
import com.zenyatta.magoya.challenge.command.BankAccountCommand.Open;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent.Opened;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent.TransactionExecuted;
import com.zenyatta.magoya.challenge.model.BankAccount;
import com.zenyatta.magoya.challenge.model.TransactionType;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankAccountService {
    public BankAccountEvent handle(final BankAccountCommand command, final BankAccount bankAccount) {
        return switch (command) {
            case Open openAccountCommand:
                yield handle(bankAccount, openAccountCommand);
            case ExecuteTransaction executeTransactionCommand:
                yield handle(bankAccount, executeTransactionCommand);
        };
    }

    private Opened handle(final BankAccount bankAccount, final Open command) {
        if (!(bankAccount instanceof BankAccount.Initial)) {
            throw new IllegalStateException("Opening bank account in '%s' status is not allowed."
                    .formatted(bankAccount.getClass().getName()));
        }

        return new Opened(
                command.bankAccountId(),
                command.clientId());
    }

    private TransactionExecuted handle(final BankAccount bankAccount, final ExecuteTransaction command) {
        if (!(bankAccount instanceof final BankAccount.Open openBankAccount)) {
            throw new IllegalStateException("Executing a transaction with an account in '%s' status is not allowed."
                    .formatted(bankAccount.getClass().getName()));
        }
        
        return new TransactionExecuted(
                command.bankAccountId(),
                command.type(),
                command.amount(),
                OffsetDateTime.now());
    }
}
