package com.victorabreu.eventos.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.victorabreu.eventos.application.port.AuditStore;
import com.victorabreu.eventos.domain.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest @ActiveProfiles("test")
class AuditRollbackTest {
    @Autowired EventService service;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean AuditStore audits;

    @Test void falhaNaAuditoriaDesfazInsercaoDoEvento() {
        doThrow(new IllegalStateException("Auditoria indisponível")).when(audits).insert(anyString(), any());
        var event = new FactoryEvent("ROLLBACK", EventType.QUALITY, "inspeção", LocalDateTime.now());
        assertThatThrownBy(() -> service.register(event)).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM factory_event WHERE event_id = 'ROLLBACK'", Integer.class)).isZero();
    }
}
