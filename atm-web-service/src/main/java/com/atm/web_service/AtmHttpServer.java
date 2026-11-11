package com.atm.web_service;

import com.atm.web_service.controladores.ATMController;
import com.atm.web_service.handlers.ErrorHandler;
import com.atm.web_service.interfaces.ApiOperation;
import com.atm.web_service.servicios.IdempotencyService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AtmHttpServer {

    private final HttpServer server;
    private final ATMController controller;
    private final Gson gson = new Gson();
    private final ErrorHandler errorHandler = new ErrorHandler();
    private final IdempotencyService idempotencyService;

    public AtmHttpServer(ATMController controller, IdempotencyService idempotencyService) throws IOException {
        this.controller = controller;
        this.idempotencyService = idempotencyService;
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        registerRoutes();
    }

    private void registerRoutes() {
        server.createContext("/api/atm/login", handleRequest(json -> {
            String nroTarjeta = json.get("nroTarjeta").getAsString();
            return controller.login(nroTarjeta);
        }, false));

        server.createContext("/api/atm/extraer", handleRequest(json -> {
            String nroTarjeta = json.get("nroTarjeta").getAsString();
            Long nroCuenta = json.get("nroCuenta").getAsLong();
            BigDecimal importe = json.get("importe").getAsBigDecimal();
            return controller.extraer(nroTarjeta, nroCuenta, importe);
        }, true));

        server.createContext("/api/atm/depositar", handleRequest(json -> {
            String nroTarjeta = json.get("nroTarjeta").getAsString();
            String cbu = json.get("cbu").getAsString();
            BigDecimal importe = json.get("importe").getAsBigDecimal();
            return controller.depositar(nroTarjeta, cbu, importe);
        }, true));

        server.createContext("/api/atm/saldo", handleRequest(json -> {
            String nroTarjeta = json.get("nroTarjeta").getAsString();
            Long nroCuenta = json.get("nroCuenta").getAsLong();
            return controller.consultarSaldo(nroTarjeta, nroCuenta);
        }, false));
    }

    private HttpHandler handleRequest(ApiOperation operation, boolean requiereIdempotencia) {
        return exchange -> {

            try {
                String body = readRequestBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                if (requiereIdempotencia) {

                    String idempotencyKey =
                            exchange.getRequestHeaders().getFirst("Idempotency-Key");

                    if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                        sendError(exchange, 400, "Falta Idempotency-Key");
                        return;
                    }

                    String path = exchange.getRequestURI().getPath();
                    String requestHash = generateRequestHash(body);

                    Object result = idempotencyService.execute(
                            idempotencyKey,
                            path,
                            requestHash,
                            () -> operation.execute(json)
                    );

                    sendResponse(exchange, 200, result);

                } else {
                    Object result = operation.execute(json);
                    sendResponse(exchange, 200, result);
                }
            } catch (Exception e) {
                String mensaje = errorHandler.handle(e);
                sendResponse(exchange, 400, mensaje);
            } finally {
                exchange.close();
            }
        };
    }

    private String generateRequestHash(String body) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(body.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            hexString.append(String.format("%02x", b & 0xff));
        }

        return hexString.toString();
    }

    private void sendError(HttpExchange exchange, int statusCode, String message)
            throws IOException {

        JsonObject error = new JsonObject();
        error.addProperty("status", statusCode);
        error.addProperty("message", message);

        byte[] response = gson.toJson(error).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(statusCode, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] response = gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        try (InputStream input = exchange.getRequestBody()) {
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        }

        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    public void start() {
        server.start();
    }
}