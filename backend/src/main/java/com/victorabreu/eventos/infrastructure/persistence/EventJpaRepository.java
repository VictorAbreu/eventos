package com.victorabreu.eventos.infrastructure.persistence;

import com.victorabreu.eventos.domain.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<EventEntity, String> {
    Page<EventEntity> findByEventType(EventType eventType, Pageable pageable);
}
