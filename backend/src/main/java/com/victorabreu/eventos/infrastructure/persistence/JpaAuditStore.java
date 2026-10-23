package com.victorabreu.eventos.infrastructure.persistence;

import com.victorabreu.eventos.application.port.AuditStore;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAuditStore implements AuditStore {
    private final EntityManager entityManager;
    public JpaAuditStore(EntityManager entityManager) { this.entityManager = entityManager; }
    @Override public void insert(String eventId, Instant recordedAt) {
        entityManager.persist(new AuditEntity(entityManager.getReference(EventEntity.class, eventId), recordedAt));
        entityManager.flush();
    }
    @Override public Optional<AuditRecord> find(String eventId) {
        return Optional.ofNullable(entityManager.find(AuditEntity.class, eventId)).map(AuditEntity::toDomain);
    }
}
