package com.victorabreu.eventos.infrastructure.persistence;

import com.victorabreu.eventos.domain.EventType;
import com.victorabreu.eventos.domain.FactoryEvent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "factory_event", indexes = @Index(name = "idx_event_time", columnList = "occurred_at,event_id"))
public class EventEntity {
    @Id @Column(name = "event_id", length = 100, nullable = false, updatable = false)
    private String eventId;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", length = 20, nullable = false)
    private EventType eventType;
    @Column(length = 2000, nullable = false)
    private String description;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime timestamp;

    protected EventEntity() { }
    EventEntity(FactoryEvent event) {
        eventId = event.eventId(); eventType = event.eventType();
        description = event.description(); timestamp = event.timestamp();
    }
    FactoryEvent toDomain() { return new FactoryEvent(eventId, eventType, description, timestamp); }
}
