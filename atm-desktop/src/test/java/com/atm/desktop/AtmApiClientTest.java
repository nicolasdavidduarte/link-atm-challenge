package com.atm.desktop;

import static org.junit.jupiter.api.Assertions.*;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

class AtmApiClientTest {

    private MockWebServer mockWebServer;
    private AtmApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        apiClient = new AtmApiClient(baseUrl.replaceAll("/$", ""));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testLoginExitoso() throws IOException, InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Ingreso exitoso"));

        // When
        String response = apiClient.login("1234-5678-9012-3456");

        // Then
        assertEquals("Ingreso exitoso", response);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/atm/login", request.getPath());
        assertTrue(request.getBody().readUtf8().contains("1234-5678-9012-3456"));
    }

    @Test
    void testExtraerEnvuelveIdempotencyKeyYParametrosCorrectos() throws IOException, InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Retire su dinero"));

        String idempotencyKey = "test-uuid-1234";

        // When
        String response = apiClient.extraer(
                "1234-5678-9012-3456",
                1001L,
                new BigDecimal("5000.00"),
                idempotencyKey
        );

        // Then
        assertEquals("Retire su dinero", response);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/atm/extraer", request.getPath());
        assertEquals(idempotencyKey, request.getHeader("Idempotency-Key"));

        String body = request.getBody().readUtf8();
        assertTrue(body.contains("5000.00"));
        assertTrue(body.contains("1001"));
    }

    @Test
    void testManejoDeErrorHttpDelServidor() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Saldo insuficiente"));

        // When & Then
        Exception exception = assertThrows(IOException.class, () -> {
            apiClient.extraer(
                    "1234-5678-9012-3456",
                    1001L,
                    new BigDecimal("999999.00"),
                    "key-error"
            );
        });

        assertTrue(exception.getMessage().contains("Saldo insuficiente"));
    }
}