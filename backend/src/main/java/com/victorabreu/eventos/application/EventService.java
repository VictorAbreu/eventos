package com.victorabreu.eventos.application;

import com.victorabreu.eventos.application.port.AuditStore;
import com.victorabreu.eventos.application.port.EventStore;
import com.victorabreu.eventos.domain.EventNotFoundException;
import com.victorabreu.eventos.domain.EventType;
import com.victorabreu.eventos.domain.FactoryEvent;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {
    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);
    private final EventStore events;
    private final AuditStore audits;
    private final Clock clock;

    public EventService(EventStore events, AuditStore audits, Clock clock) {
        this.events = events;
        this.audits = audits;
        this.clock = clock;
    }

    @Transactional
    public FactoryEvent register(FactoryEvent event) {
        events.insert(event);
        if (event.requiresAudit()) {
            audits.insert(event.eventId(), clock.instant());
        }
        LOG.debug("Registro de evento preparado na transação: tipo={}", event.eventType());
        return event;
    }

    public FactoryEvent find(String eventId) {
        return events.find(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

    public EventStore.EventPage list(EventType type, int page, int size) {
        return events.list(type, page, size);
    }

    public Optional<AuditStore.AuditRecord> audit(String eventId) {
        find(eventId);
        return audits.find(eventId);
    }
}
