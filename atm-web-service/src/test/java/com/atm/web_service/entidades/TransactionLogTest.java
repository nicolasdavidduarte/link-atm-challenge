package com.atm.web_service.entidades;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionLogTest {

    @Test
    void testToCsvLineConImporteYCuenta() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 10, 16, 0, 0);
        TransactionLog tx = TransactionLog.crear(
                fecha,
                "TX-123456",
                "1234-5678-9012-3456",
                1001L,
                null,
                "EXTRACCIÓN",
                new BigDecimal("5000.00"),
                true,
                "TEST"
        );

        String csvLine = tx.toCsvLine();

        assertTrue(csvLine.contains("TX-123456"));
        assertTrue(csvLine.contains("EXTRACCIÓN"));
        assertTrue(csvLine.contains("1234-5678-9012-3456"));
        assertTrue(csvLine.contains("1001"));
        assertTrue(csvLine.contains("5000.00"));
        assertTrue(csvLine.contains("true"));
    }

    @Test
    void testToCsvLineSinImporteNiCuentaLogin() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 10, 16, 0, 0);
        TransactionLog tx = TransactionLog.crear(
                fecha,
                "TX-789012",
                "1234-5678-9012-3456",
                null,
                null,
                "LOGIN",
                null,
                true,
                "TEST"
        );

        String csvLine = tx.toCsvLine();

        assertTrue(csvLine.contains("TX-789012"));
        assertTrue(csvLine.contains("LOGIN"));
        assertTrue(csvLine.contains("1234-5678-9012-3456"));
        assertTrue(csvLine.contains("true"));
    }
}