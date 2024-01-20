package com.zenyatta.magoya.challenge.event;

import com.zenyatta.magoya.challenge.event.util.EventEnvelope;

public interface EventBus {
    <EventT> void publish(EventEnvelope<EventT> event);
}
  