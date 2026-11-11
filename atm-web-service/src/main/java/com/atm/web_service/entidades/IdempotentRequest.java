package com.atm.web_service.entidades;

import java.time.LocalDateTime;

public class IdempotentRequest {

    public enum Estado {
        PROCESSING, COMPLETED
    }

    private final String idempotencyKey;
    private final String operation;
    private final String requestHash;
    private volatile Estado estado;
    private volatile Object response;
    private final LocalDateTime fecha;

    public IdempotentRequest(String idempotencyKey, String operation, String requestHash) {
        this.idempotencyKey = idempotencyKey;
        this.operation = operation;
        this.requestHash = requestHash;
        this.estado = Estado.PROCESSING;
        this.fecha = LocalDateTime.now();
    }

    public synchronized void completar(Object response) {
        this.response = response;
        this.estado = Estado.COMPLETED;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getOperation() {
        return operation;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public Estado getEstado() {
        return estado;
    }

    public Object getResponse() {
        return response;
    }

    public LocalDateTime getFecha() { return fecha;}

}