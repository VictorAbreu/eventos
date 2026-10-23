package com.victorabreu.eventos.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.victorabreu.eventos.application.port.*;
import com.victorabreu.eventos.domain.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;

class EventServiceTest {
    private final EventStore events = mock(EventStore.class);
    private final AuditStore audits = mock(AuditStore.class);
    private final Instant now = Instant.parse("2026-05-20T13:00:00Z");
    private final EventService service = new EventService(events, audits, Clock.fixed(now, ZoneOffset.UTC));

    @Test void producaoNaoGeraAuditoria() {
        var event = event(EventType.PRODUCTION);
        assertThat(service.register(event)).isEqualTo(event);
        verify(events).insert(event);
        verifyNoInteractions(audits);
    }
    @Test void qualidadeGeraAuditoriaAposEventoComHorarioDoServidor() {
        var event = event(EventType.QUALITY);
        service.register(event);
        var order = inOrder(events, audits);
        order.verify(events).insert(event);
        order.verify(audits).insert(event.eventId(), now);
    }
    @Test void duplicidadeNaoGeraAuditoria() {
        var event = event(EventType.QUALITY);
        doThrow(new DuplicateEventException(event.eventId())).when(events).insert(event);
        assertThatThrownBy(() -> service.register(event)).isInstanceOf(DuplicateEventException.class);
        verifyNoInteractions(audits);
    }
    @Test void eventoInexistenteNaoPossuiAuditoria() {
        when(events.find("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.audit("missing")).isInstanceOf(EventNotFoundException.class);
        verifyNoInteractions(audits);
    }
    @Test void consultasDelegamParaContratosDePersistencia() {
        var event = event(EventType.QUALITY);
        var audit = new AuditStore.AuditRecord("EVT-1", now, "QUALITY_EVENT_REGISTERED");
        when(events.find("EVT-1")).thenReturn(Optional.of(event));
        when(audits.find("EVT-1")).thenReturn(Optional.of(audit));
        var page = new EventStore.EventPage(List.of(event), 1, 1, 0, 10);
        when(events.list(EventType.QUALITY, 0, 10)).thenReturn(page);
        assertThat(service.find("EVT-1")).isEqualTo(event);
        assertThat(service.audit("EVT-1")).contains(audit);
        assertThat(service.list(EventType.QUALITY, 0, 10)).isEqualTo(page);
    }
    private FactoryEvent event(EventType type) {
        return new FactoryEvent("EVT-1", type, "Evento do equipamento", LocalDateTime.of(2026, 5, 20, 10, 0));
    }
}
