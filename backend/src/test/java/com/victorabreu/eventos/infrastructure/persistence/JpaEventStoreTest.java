package com.victorabreu.eventos.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.victorabreu.eventos.domain.*;
import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

class JpaEventStoreTest {
    private final EntityManager manager = mock(EntityManager.class);
    private final JpaEventStore store = new JpaEventStore(manager, mock(EventJpaRepository.class));
    private final FactoryEvent event = new FactoryEvent("id", EventType.PRODUCTION, "Iniciado", LocalDateTime.now());

    @Test void traduzSomenteViolacoesDeUnicidade() {
        var sql = new SQLException("Violação de unicidade", "23505");
        var unique = new ConstraintViolationException("Duplicado", sql, "PRIMARY_KEY");
        doThrow(new IllegalStateException(unique)).when(manager).persist(any());
        assertThatThrownBy(() -> store.insert(event)).isInstanceOf(DuplicateEventException.class).hasMessageContaining("id");
    }
    @Test void preservaFalhasSemDuplicidade() {
        var failure = new ConstraintViolationException("Chave estrangeira inválida", new SQLException("FK", "23506"), "FK");
        doThrow(failure).when(manager).persist(any());
        assertThatThrownBy(() -> store.insert(event)).isSameAs(failure);
    }
    @Test void preservaFalhasSemCausaSql() {
        var failure = new IllegalStateException("Indisponível");
        doThrow(failure).when(manager).persist(any());
        assertThatThrownBy(() -> store.insert(event)).isSameAs(failure);
    }
}
