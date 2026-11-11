package com.atm.web_service.servicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService();
    }

    @Test
    void testEjecucionUnicaEIdempotencia() throws Exception {
        String key = "key-123";
        String operation = "/api/atm/extraer";
        String hash = "hash-abc";

        AtomicInteger executionCount = new AtomicInteger(0);

        Object result1 = idempotencyService.execute(key, operation, hash, () -> {
            executionCount.incrementAndGet();
            return "ResultadoExitoso";
        });

        Object result2 = idempotencyService.execute(key, operation, hash, () -> {
            executionCount.incrementAndGet();
            return "ResultadoReejecutado";
        });

        assertEquals("ResultadoExitoso", result1);
        assertEquals("ResultadoExitoso", result2);
        assertEquals(1, executionCount.get(), "La acción de negocio solo se ejecuta una vez");
    }

    @Test
    void testConcurrenciaSimultaneaMismaKey() throws Exception {
        String key = "key-concurrente";
        String operation = "/api/atm/depositar";
        String hash = "hash-xyz";

        AtomicInteger executionCount = new AtomicInteger(0);
        int hilos = 5;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger erroresConcurrencia = new AtomicInteger(0);

        for (int i = 0; i < hilos; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    idempotencyService.execute(key, operation, hash, () -> {
                        Thread.sleep(100);
                        executionCount.incrementAndGet();
                        return "OK";
                    });
                    exitos.incrementAndGet();
                } catch (IllegalStateException e) {
                    erroresConcurrencia.incrementAndGet();
                } catch (Exception e) {
                    fail("No debería lanzar otra excepción: " + e.getMessage());
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.yield();
        }

        assertEquals(1, executionCount.get(), "La lógica de negocio se ejecutó en paralelo de más");
    }

    @Test
    void testReutilizacionKeyConDistintoRequestGeneraError() throws Exception {
        String key = "key-reutilizada";

        idempotencyService.execute(key, "/api/atm/saldo", "hash-1", () -> "Saldo");

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            idempotencyService.execute(key, "/api/atm/extraer", "hash-2", () -> "Extraer");
        });

        assertEquals("Idempotency-Key reutilizada con otro request", exception.getMessage());
    }
}