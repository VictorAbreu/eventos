package com.victorabreu.eventos.api.dto;

import com.victorabreu.eventos.domain.EventType;
import com.victorabreu.eventos.domain.FactoryEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Informe o ID do evento.")
        @Size(max = 100, message = "O ID deve conter até 100 caracteres.")
        @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]*", message = "Use letras, números, pontos, hífens ou sublinhados, iniciando por letra ou número.")
        @Schema(example = "EVT-1001") String eventId,
        @NotNull(message = "Informe o tipo do evento.") @Schema(example = "PRODUCTION") EventType eventType,
        @NotBlank(message = "Informe a descrição do evento.")
        @Size(max = 2000, message = "A descrição deve conter até 2.000 caracteres.")
        @Schema(example = "Linha de produção iniciada") String description,
        @NotNull(message = "Informe a data e hora da ocorrência.") @Schema(example = "2026-05-20T10:00:00") LocalDateTime timestamp) {
    public FactoryEvent toDomain() { return new FactoryEvent(eventId, eventType, description, timestamp); }
}
