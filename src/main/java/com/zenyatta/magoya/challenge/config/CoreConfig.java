package com.zenyatta.magoya.challenge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenyatta.magoya.challenge.event.EventBus;
import com.zenyatta.magoya.challenge.event.EventSerializer;
import com.zenyatta.magoya.challenge.event.util.EventForwarder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CoreConfig {

    @Bean
    ObjectMapper defaultJsonMapper() {
        return EventSerializer.mapper;
    }

    @Bean
    EventBus eventBus(final ApplicationEventPublisher applicationEventPublisher) {
        return new EventForwarder(applicationEventPublisher);
    }
    
}
