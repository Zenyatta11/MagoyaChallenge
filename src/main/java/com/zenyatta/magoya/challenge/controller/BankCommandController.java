package com.zenyatta.magoya.challenge.controller;

import static com.eventstore.dbclient.ExpectedRevision.expectedRevision;
import static com.eventstore.dbclient.ExpectedRevision.noStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zenyatta.magoya.challenge.command.BankAccountCommand;
import com.zenyatta.magoya.challenge.dto.request.AccountQueryRequest;
import com.zenyatta.magoya.challenge.dto.request.BankAccountRequest;
import com.zenyatta.magoya.challenge.event.EventStoreHandler;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent;
import com.zenyatta.magoya.challenge.model.BankAccount;
import com.zenyatta.magoya.challenge.model.TransactionType;
import com.zenyatta.magoya.challenge.repository.BankAccountDetailsRepository;
import com.zenyatta.magoya.challenge.utils.http.ETag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
class BankCommandController {
    private final EventStoreHandler<BankAccount, BankAccountCommand, BankAccountEvent> store;
    private final BankAccountDetailsRepository bankRepository;

    private static ObjectMapper MAPPER = new ObjectMapper();

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<String> open(
            @Valid @RequestBody final BankAccountRequest.Open request) throws JsonProcessingException {
        UUID clientId;

        if (request.clientId() == null) {
            clientId = UUID.randomUUID();
        } else {
            clientId = request.clientId();
        }

        final UUID accountId = UUID.randomUUID();

        final ETag result = store.handle(
                accountId,
                new BankAccountCommand.Open(
                        accountId,
                        clientId),
                noStream());

        final ObjectNode jsonNode = MAPPER.createObjectNode();

        jsonNode.put("account_id", accountId.toString());
        jsonNode.put("client_id", clientId.toString());

        final String jsonString = MAPPER.writeValueAsString(jsonNode);

        return ResponseEntity
            .ok()
            .eTag(result.value())
            .body(jsonString);
    }

    @PostMapping("/transactions")
    ResponseEntity<Void> executeTransaction(
            @RequestBody final BankAccountRequest.ExecuteTransaction request,
            @RequestHeader(name = HttpHeaders.IF_MATCH) @Parameter(in = ParameterIn.HEADER, required = true, schema = @Schema(type = "string")) @NotNull final ETag ifMatch) {
            
        if (request.amount() <= 0) {
            throw new IllegalArgumentException("The movement amount must be greater than zero.");
        }

        final double balance = new AccountQueryRequest(request.bankAccountId(), null, bankRepository).handle().getBalance();

        if(balance < request.amount() && request.type() == TransactionType.WITHDRAWAL) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        final ETag result = store.handle(
                request.bankAccountId(),
                new BankAccountCommand.ExecuteTransaction(
                        request.bankAccountId(),
                        request.type(),
                        request.amount()),
                expectedRevision(ifMatch.toLong()));

        return ResponseEntity
                .ok()
                .eTag(result.value())
                .build();
    }

}
