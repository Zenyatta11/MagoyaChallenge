package com.zenyatta.magoya.challenge.dto.request;

import com.zenyatta.magoya.challenge.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;

public final class BankAccountRequest {

    public record Open(UUID clientId) {
    }

    @Validated
    public record ExecuteTransaction(
            @NotNull UUID bankAccountId,
            @NotNull TransactionType type,
            @NotNull double amount
    ) {
    }

}
