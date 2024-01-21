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

/**
 * This class is the main controller for all command endpoints.
 * It is in charge of validation and delegating the requested command
 * to the command processor.
 */
@Validated
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
class BankCommandController {
    private final EventStoreHandler<BankAccount, BankAccountCommand, BankAccountEvent> store;
    private final BankAccountDetailsRepository bankRepository;

    private static ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Creates a new account. If a client ID is provided, the new bank account
     * will be opened under said client ID. If it is not provided, a new client ID
     * will be created and the account will be set under the newly generated client
     * ID.
     * </p>
     * The newly generated account will have a version of 0 and a balance of $0. Once
     * the command is prepared, it is sent to the command store.

     * @param request   The request object received
     * @return JSON     A JSON object of the account details
     * @see EventStoreHandler
     */
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

    /**
     * This endpoint is in charge of executing transactions, be it
     * <code>DEPOSIT</code> or <code>WITHDRAWAL</code>. If the action
     * is a <code>WITHDRAWAL</code>, it will first check if there are
     * sufficient funds in the account and that we are not working with
     * a negative movement amount.

     * @param request   The request object
     * @param IF_MATCH  The versioning header. Should it not be equal to the latest version
     *                  the command will return 412.
     * @return void     The success is sent via HTTP status code
     * @see EventHandlerStore
     */
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
