package com.victorabreu.eventos.api;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.victorabreu.eventos.application.EventService;
import com.victorabreu.eventos.domain.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest @ActiveProfiles("test")
class EventApiTest {
    MockMvc mvc;
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired EventService service;

    @BeforeEach void clean() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        jdbc.update("DELETE FROM event_audit"); jdbc.update("DELETE FROM factory_event");
    }

    @Test void registraEConsultaEventoDeProducao() throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(payload("EVT-1001", "PRODUCTION")))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/events/EVT-1001"))
                .andExpect(jsonPath("$.eventId").value("EVT-1001"));
        mvc.perform(get("/api/events/EVT-1001")).andExpect(status().isOk()).andExpect(jsonPath("$.eventType").value("PRODUCTION"));
        mvc.perform(get("/api/events/EVT-1001/audit")).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event_audit", Integer.class)).isZero();
    }
    @Test void registroDeQualidadeEAuditoriaSaoAtomicosMesmoComDuplicidade() throws Exception {
        var json = payload("Q-1", "QUALITY");
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated());
        mvc.perform(get("/api/events/Q-1/audit")).andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("Q-1"))
                .andExpect(jsonPath("$.action").value("QUALITY_EVENT_REGISTERED"))
                .andExpect(jsonPath("$.recordedAt").isNotEmpty());
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(json.replace("Evento do equipamento", "Descrição alterada")))
                .andExpect(status().isConflict()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Evento duplicado"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM factory_event", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event_audit", Integer.class)).isEqualTo(1);
        assertThat(service.find("Q-1").description()).isEqualTo("Evento do equipamento");
    }
    @Test void paginacaoEFiltroPossuemOrdenacaoEstavel() throws Exception {
        service.register(new FactoryEvent("B", EventType.PRODUCTION, "b", LocalDateTime.of(2026, 5, 20, 10, 0)));
        service.register(new FactoryEvent("A", EventType.QUALITY, "a", LocalDateTime.of(2026, 5, 20, 10, 0)));
        mvc.perform(get("/api/events").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2)).andExpect(jsonPath("$.content[0].eventId").value("A"));
        mvc.perform(get("/api/events").param("eventType", "PRODUCTION"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].eventId").value("B"));
        mvc.perform(get("/api/events").param("page", "3")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }
    @ParameterizedTest @ValueSource(strings = {"{}", "null", "{", "{\"eventId\":\"id\",\"eventType\":\"OTHER\"}",
            "{\"eventId\":\"a/b\",\"eventType\":\"QUALITY\",\"description\":\"ok\",\"timestamp\":\"2026-05-20T10:00:00\"}"})
    void dadosInvalidosRetornam400(String json) throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM factory_event", Integer.class)).isZero();
    }
    @Test void informaErrosDosCamposEmPortugues() throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.eventId").value("Informe o ID do evento."))
                .andExpect(jsonPath("$.errors.eventType").value("Informe o tipo do evento."))
                .andExpect(jsonPath("$.errors.description").value("Informe a descrição do evento."))
                .andExpect(jsonPath("$.errors.timestamp").value("Informe a data e hora da ocorrência."));
    }
    @ParameterizedTest @ValueSource(strings = {"page=-1", "size=0", "size=101", "page=text", "eventType=OTHER"})
    void consultaInvalidaRetorna400(String query) throws Exception {
        mvc.perform(get("/api/events?" + query)).andExpect(status().isBadRequest());
    }
    @Test void eventoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/events/missing")).andExpect(status().isNotFound());
        mvc.perform(get("/api/events/missing/audit")).andExpect(status().isNotFound());
    }
    @Test void rejeitaMetodosNaoPermitidosECamposJsonDesconhecidos() throws Exception {
        mvc.perform(delete("/api/events/EVT-1")).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Método não permitido"));
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(payload("EVT-1", "PRODUCTION").replace("\"description\":", "\"unknownField\":1,\"description\":")))
                .andExpect(status().isBadRequest());
    }
    @Test void disponibilizaDocumentacaoESaude() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Eventos Industriais"))
                .andExpect(jsonPath("$.paths['/api/events'].post").exists());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
    @Test void duplicidadeConcorrenteCriaSomenteUmEventoEUmaAuditoria() throws Exception {
        int workers = 8;
        var ready = new CountDownLatch(workers);
        var start = new CountDownLatch(1);
        var event = new FactoryEvent("RACE", EventType.QUALITY, "Registro concorrente", LocalDateTime.now());
        try (var executor = Executors.newFixedThreadPool(workers)) {
            var tasks = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < workers; i++) {
                tasks.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Tempo esgotado para iniciar");
                    try { service.register(event); return true; }
                    catch (DuplicateEventException expected) { return false; }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            var results = new ArrayList<Boolean>();
            for (var task : tasks) results.add(task.get(20, TimeUnit.SECONDS));
            assertThat(results.stream().filter(Boolean::booleanValue).count()).isEqualTo(1);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM factory_event", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event_audit", Integer.class)).isEqualTo(1);
    }
    private String payload(String id, String type) {
        return """
                {"eventId":"%s","eventType":"%s","description":"Evento do equipamento","timestamp":"2026-05-20T10:00:00"}
                """.formatted(id, type);
    }
}
