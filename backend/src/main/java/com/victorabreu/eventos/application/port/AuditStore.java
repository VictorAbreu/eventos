package com.victorabreu.eventos.application.port;

import java.time.Instant;
import java.util.Optional;

public interface AuditStore {
    void insert(String eventId, Instant recordedAt);
    Optional<AuditRecord> find(String eventId);

    record AuditRecord(String eventId, Instant recordedAt, String action) { }
}
