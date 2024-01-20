package com.zenyatta.magoya.challenge.projection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent;
import com.zenyatta.magoya.challenge.event.util.EventEnvelope;
import com.zenyatta.magoya.challenge.model.BankAccountDetails;
import com.zenyatta.magoya.challenge.model.TransactionType;
import com.zenyatta.magoya.challenge.repository.BankAccountDetailsRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class BankAccountDetailsProjection extends JPAProjection<BankAccountDetails, UUID> {
    private static ObjectMapper MAPPER = new ObjectMapper();

    protected BankAccountDetailsProjection(final BankAccountDetailsRepository repository) {
        super(repository);
    }

    @EventListener
    void handleBankAccountOpened(final EventEnvelope<BankAccountEvent.Opened> eventEnvelope) {
        add(eventEnvelope, () -> {
            final BankAccountEvent.Opened event = eventEnvelope.data();

            return new BankAccountDetails(
                    event.bankAccountId(),
                    event.clientId(),
                    0,
                    eventEnvelope.metadata().streamPosition(),
                    eventEnvelope.metadata().logPosition());
        });
    }

    @EventListener
    void handleTransactionExecuted(final EventEnvelope<BankAccountEvent.TransactionExecuted> eventEnvelope) {
        log.info("Handle transaction executed event");

        getAndUpdate(eventEnvelope.data().bankAccountId(), eventEnvelope, view -> {
            final BankAccountEvent.TransactionExecuted event = eventEnvelope.data();

            double delta = 0;

            if (event.type() == TransactionType.DEPOSIT) {
                delta = event.amount();
            } else if (event.type() == TransactionType.WITHDRAWAL) {
                delta = -event.amount();
            }

            if (event.amount() >= 10000 && log.isInfoEnabled()) {

                try {
                    final ObjectNode jsonNode = MAPPER.createObjectNode();

                    jsonNode.put("bank_account_id", event.bankAccountId().toString());
                    jsonNode.put("amount", event.amount());
                    jsonNode.put("type", event.type().toString());

                    final String jsonString = MAPPER.writeValueAsString(jsonNode);

                    System.out.println(jsonString);
                } catch (final JsonProcessingException exception) {
                    log.warn("An exception occured when informing high value transaction", exception);
                    log.info("A movement of more than $10.000 USD has been made!");
                }
            }

            view.setBalance(view.getBalance() + delta);

            return view;
        });
    }
}
