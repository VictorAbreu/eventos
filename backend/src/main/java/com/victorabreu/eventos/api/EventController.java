package com.victorabreu.eventos.api;

import com.victorabreu.eventos.api.dto.CreateEventRequest;
import com.victorabreu.eventos.application.EventService;
import com.victorabreu.eventos.application.port.AuditStore.AuditRecord;
import com.victorabreu.eventos.application.port.EventStore.EventPage;
import com.victorabreu.eventos.domain.EventType;
import com.victorabreu.eventos.domain.FactoryEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Registro e consulta de eventos de equipamentos")
public class EventController {
    private final EventService service;
    public EventController(EventService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Registra evento; QUALITY também gera auditoria", description = "201: criado; 400: inválido; 409: ID já registrado. Nenhum evento existente é sobrescrito.")
    public ResponseEntity<FactoryEvent> create(@Valid @RequestBody CreateEventRequest request) {
        var event = service.register(request.toDomain());
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{eventId}").buildAndExpand(event.eventId()).toUri();
        return ResponseEntity.created(location).body(event);
    }

    @GetMapping
    @Operation(summary = "Lista eventos com paginação e filtro opcional por tipo")
    public EventPage list(@RequestParam(required = false) EventType eventType,
                          @RequestParam(defaultValue = "0") @Min(0) int page,
                          @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return service.list(eventType, page, size);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Consulta evento por ID")
    public FactoryEvent find(@PathVariable String eventId) { return service.find(eventId); }

    @GetMapping("/{eventId}/audit")
    @Operation(summary = "Consulta auditoria: 200 para QUALITY, 204 para PRODUCTION, 404 se evento inexistente")
    public ResponseEntity<AuditRecord> audit(@PathVariable String eventId) {
        return service.audit(eventId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
