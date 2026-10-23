package com.victorabreu.eventos.domain;

public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException(String eventId) {
        super("O evento '" + eventId + "' já foi registrado.");
    }
}
