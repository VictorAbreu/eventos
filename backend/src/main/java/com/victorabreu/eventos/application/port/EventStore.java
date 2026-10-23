package com.victorabreu.eventos.application.port;

import com.victorabreu.eventos.domain.EventType;
import com.victorabreu.eventos.domain.FactoryEvent;
import java.util.List;
import java.util.Optional;

public interface EventStore {
    void insert(FactoryEvent event);
    Optional<FactoryEvent> find(String eventId);
    EventPage list(EventType type, int page, int size);

    record EventPage(List<FactoryEvent> content, long totalElements, int totalPages, int number, int size) {
        public EventPage { content = List.copyOf(content); }
    }
}
