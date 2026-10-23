package com.victorabreu.eventos.domain;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String eventId) { super("Evento '" + eventId + "' não encontrado."); }
}
