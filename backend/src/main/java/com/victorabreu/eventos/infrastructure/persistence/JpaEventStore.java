package com.victorabreu.eventos.infrastructure.persistence;

import com.victorabreu.eventos.application.port.EventStore;
import com.victorabreu.eventos.domain.DuplicateEventException;
import com.victorabreu.eventos.domain.EventType;
import com.victorabreu.eventos.domain.FactoryEvent;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class JpaEventStore implements EventStore {
    private final EntityManager entityManager;
    private final EventJpaRepository repository;

    public JpaEventStore(EntityManager entityManager, EventJpaRepository repository) {
        this.entityManager = entityManager; this.repository = repository;
    }

    @Override
    public void insert(FactoryEvent event) {
        try {
            entityManager.persist(new EventEntity(event));
            entityManager.flush();
        } catch (RuntimeException exception) {
            if (isUniqueViolation(exception)) { throw new DuplicateEventException(event.eventId()); }
            throw exception;
        }
    }

    private boolean isUniqueViolation(Throwable exception) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current instanceof ConstraintViolationException violation && "23505".equals(violation.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    @Override public Optional<FactoryEvent> find(String eventId) { return repository.findById(eventId).map(EventEntity::toDomain); }

    @Override public EventPage list(EventType type, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("eventId")));
        var result = type == null ? repository.findAll(pageable) : repository.findByEventType(type, pageable);
        return new EventPage(result.map(EventEntity::toDomain).getContent(), result.getTotalElements(),
                result.getTotalPages(), result.getNumber(), result.getSize());
    }
}
