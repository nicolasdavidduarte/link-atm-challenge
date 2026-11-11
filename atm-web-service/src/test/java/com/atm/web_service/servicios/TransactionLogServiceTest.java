package com.atm.web_service.servicios;

import com.atm.web_service.entidades.TransactionLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionLogServiceTest {

    @Test
    void testLoggearTransaccionCreaArchivoYEscribeCsv(@TempDir Path tempDir) throws IOException {
        // Given
        TransactionLog tx = TransactionLog.crear(
                LocalDateTime.of(2026, 11, 10, 16, 0, 0),
                "TX-999999",
                "1234-5678-9012-3456",
                1001L,
                null,
                "EXTRACCIÓN",
                new BigDecimal("1500.00"),
                true,
                "TEST"
        );

        TransactionLogService service = new TransactionLogService();

        // When
        service.loggearTransacccion(tx);

        // Then
        Path defaultLogFile = Paths.get("log_transacciones.csv");
        assertTrue(Files.exists(defaultLogFile), "El archivo de log CSV debe haberse creado");

        List<String> lines = Files.readAllLines(defaultLogFile);
        assertFalse(lines.isEmpty(), "El archivo no debe estar vacío");

        boolean encontrada = lines.stream().anyMatch(line -> line.contains("TX-999999"));
        assertTrue(encontrada, "El log debe registrar la transacción realizada");

        Files.deleteIfExists(defaultLogFile);
    }
}