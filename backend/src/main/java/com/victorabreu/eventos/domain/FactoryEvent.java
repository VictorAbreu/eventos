package com.victorabreu.eventos.domain;

import java.time.LocalDateTime;

public record FactoryEvent(String eventId, EventType eventType, String description, LocalDateTime timestamp) {
    public FactoryEvent {
        if (eventId == null || eventId.isBlank() || eventId.length() > 100) {
            throw new IllegalArgumentException("O ID deve conter entre 1 e 100 caracteres.");
        }
        if (eventType == null || timestamp == null) {
            throw new IllegalArgumentException("Tipo e data/hora são obrigatórios.");
        }
        if (description == null || description.isBlank() || description.length() > 2000) {
            throw new IllegalArgumentException("A descrição deve conter entre 1 e 2000 caracteres.");
        }
        eventId = eventId.strip();
        description = description.strip();
    }

    public boolean requiresAudit() { return eventType == EventType.QUALITY; }
}
