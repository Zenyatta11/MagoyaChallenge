package com.zenyatta.magoya.challenge.event.util;

import com.zenyatta.magoya.challenge.event.EventBus;
import org.springframework.context.ApplicationEventPublisher;

public record EventForwarder(ApplicationEventPublisher applicationEventPublisher) implements EventBus {

    @Override
    public <EventT> void publish(final EventEnvelope<EventT> event) {
        applicationEventPublisher.publishEvent(event);
    }
    
}
