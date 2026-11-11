package com.atm.web_service.servicios;

import com.atm.web_service.entidades.IdempotentRequest;
import com.atm.web_service.interfaces.IdempotentOperation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IdempotencyService {

    private final Map<String, IdempotentRequest> requests = new ConcurrentHashMap<>();

    public Object execute(
            String key,
            String operation,
            String requestHash,
            IdempotentOperation idempotentOperation
    ) throws Exception {

        IdempotentRequest newRequest = new IdempotentRequest(key, operation, requestHash);
        IdempotentRequest existing = requests.putIfAbsent(key, newRequest);

        if (existing != null) {
            if (!existing.getOperation().equals(operation) || !existing.getRequestHash().equals(requestHash)) {
                throw new IllegalStateException("Idempotency-Key reutilizada con otro request");
            }

            if (existing.getEstado() == IdempotentRequest.Estado.PROCESSING) {
                throw new IllegalStateException("El request anterior aún se está procesando");
            }

            return existing.getResponse();
        }

        try {
            Object response = idempotentOperation.execute();

            newRequest.completar(response);

            return response;
        } catch (Exception e) {
            requests.remove(key);
            throw e;
        }
    }
}