package com.victorabreu.eventos.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import com.victorabreu.eventos.application.port.AuditStore.AuditRecord;

@Entity
@Table(name = "event_audit")
public class AuditEntity {
    @Id @Column(name = "event_id", length = 100)
    private String eventId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @MapsId
    @JoinColumn(name = "event_id", foreignKey = @ForeignKey(name = "fk_audit_event"))
    private EventEntity event;
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
    @Column(nullable = false, length = 40)
    private String action;

    protected AuditEntity() { }
    AuditEntity(EventEntity event, Instant recordedAt) {
        this.event = event; this.recordedAt = recordedAt; this.action = "QUALITY_EVENT_REGISTERED";
    }
    AuditRecord toDomain() { return new AuditRecord(eventId, recordedAt, action); }
}
