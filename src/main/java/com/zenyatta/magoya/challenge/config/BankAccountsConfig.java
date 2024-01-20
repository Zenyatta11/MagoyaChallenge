package com.zenyatta.magoya.challenge.config;

import com.eventstore.dbclient.EventStoreDBClient;
import com.zenyatta.magoya.challenge.command.BankAccountCommand;
import com.zenyatta.magoya.challenge.event.EventStoreHandler;
import com.zenyatta.magoya.challenge.event.domain.BankAccountEvent;
import com.zenyatta.magoya.challenge.model.BankAccount;
import com.zenyatta.magoya.challenge.service.BankAccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.ApplicationScope;

@Configuration
class BankAccountsConfig {
    @Bean
    BankAccountService bankAccountService() {
        return new BankAccountService();
    }

    @Bean
    @ApplicationScope
    EventStoreHandler<BankAccount, BankAccountCommand, BankAccountEvent> bankAccountStore(
            final EventStoreDBClient eventStore,
            final BankAccountService decider) {
        return new EventStoreHandler<>(
                eventStore,
                BankAccount::when,
                decider::handle,
                BankAccount::mapToStreamId,
                BankAccount::empty);
    }
}
