package com.victorabreu.eventos.domain;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FactoryEventTest {
    private static final LocalDateTime TIME = LocalDateTime.of(2026, 5, 20, 10, 0);

    @Test void normalizaTextosEIdentificaRegraDeAuditoria() {
        var event = new FactoryEvent(" EVT-1 ", EventType.QUALITY, " inspeção ", TIME);
        assertThat(event.eventId()).isEqualTo("EVT-1");
        assertThat(event.description()).isEqualTo("inspeção");
        assertThat(event.requiresAudit()).isTrue();
        assertThat(new FactoryEvent("EVT-2", EventType.PRODUCTION, "produção", TIME).requiresAudit()).isFalse();
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t"})
    void rejeitaIdentificadoresInvalidos(String id) {
        assertThatIllegalArgumentException().isThrownBy(() -> new FactoryEvent(id, EventType.PRODUCTION, "ok", TIME));
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t"})
    void rejeitaDescricoesInvalidas(String description) {
        assertThatIllegalArgumentException().isThrownBy(() -> new FactoryEvent("id", EventType.PRODUCTION, description, TIME));
    }
    @Test void rejeitaValoresAusentesOuAcimaDoLimite() {
        assertThatIllegalArgumentException().isThrownBy(() -> new FactoryEvent("x".repeat(101), EventType.PRODUCTION, "ok", TIME));
        assertThatIllegalArgumentException().isThrownBy(() -> new FactoryEvent("id", null, "ok", TIME));
        assertThatIllegalArgumentException().isThrownBy(() -> new FactoryEvent("id", EventType.PRODUCTION, "ok", null));
        assertThatIllegalArgumentException().isThrownBy(() -> new FactoryEvent("id", EventType.PRODUCTION, "x".repeat(2001), TIME));
    }
}
