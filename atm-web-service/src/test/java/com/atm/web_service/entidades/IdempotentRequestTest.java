package com.atm.web_service.entidades;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdempotentRequestTest {

    @Test
    void testCreacionYCompletadoDeRequest() {
        IdempotentRequest request = new IdempotentRequest("key-1", "/api/atm/extraer", "hash-1");

        assertEquals("key-1", request.getIdempotencyKey());
        assertEquals("/api/atm/extraer", request.getOperation());
        assertEquals("hash-1", request.getRequestHash());
        assertEquals(IdempotentRequest.Estado.PROCESSING, request.getEstado());
        assertNull(request.getResponse());

        request.completar("Operacion Exitosa");

        assertEquals(IdempotentRequest.Estado.COMPLETED, request.getEstado());
        assertEquals("Operacion Exitosa", request.getResponse());
    }
}