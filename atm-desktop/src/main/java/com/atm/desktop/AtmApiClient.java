package com.atm.desktop;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AtmApiClient {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public AtmApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String login(String nroTarjeta) throws IOException {
        return post("/api/atm/login",
                Collections.singletonMap("nroTarjeta", nroTarjeta), null);
    }

    public String extraer(
            String nroTarjeta,
            Long nroCuenta,
            BigDecimal importe,
            String idempotencyKey) throws IOException {

        Map<String, Object> request = new HashMap<>();

        request.put("nroTarjeta", nroTarjeta);
        request.put("nroCuenta", nroCuenta);
        request.put("importe", importe);

        return post("/api/atm/extraer", request, idempotencyKey);
    }

    public String depositar(
            String nroTarjeta,
            String cbu,
            BigDecimal importe,
            String idempotencyKey) throws IOException {

        Map<String, Object> request = new HashMap<>();

        request.put("nroTarjeta", nroTarjeta);
        request.put("cbu", cbu);
        request.put("importe", importe);

        return post("/api/atm/depositar", request, idempotencyKey);
    }

    public String saldo(
            String nroTarjeta,
            Long nroCuenta
    )throws IOException {
        Map<String, Object> request = new HashMap<>();

        request.put("nroTarjeta", nroTarjeta);
        request.put("nroCuenta", nroCuenta);

        return post("/api/atm/saldo", request, null);
    }

    private String post(String path, Object body, String key) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)
                new URL(baseUrl + path).openConnection();

        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Idempotency-Key", key);

        try (OutputStream out = connection.getOutputStream()) {
            out.write(gson.toJson(body).getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();

        String response = getResponse(status, connection);
        connection.disconnect();

        if (status >= 400)
            throw new IOException(response);

        return response;
    }

    private static String getResponse(int status, HttpURLConnection connection) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;

        try (InputStream input = status < 400
                ? connection.getInputStream()
                : connection.getErrorStream()) {

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        return new String(
                output.toByteArray(), StandardCharsets.UTF_8);
    }
}