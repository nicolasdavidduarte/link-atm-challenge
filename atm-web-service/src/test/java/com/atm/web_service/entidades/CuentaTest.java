package com.atm.web_service.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaTest {

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cuenta = Cuenta.crear(1001L, "0110001330000012345678", new BigDecimal("10000.00"));
    }

    @Test
    void testDepositarMontoValidoIncrementaSaldo() {
        cuenta.depositar(new BigDecimal("2500.00"));
        assertEquals(0, new BigDecimal("12500.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    void testDepositarMontoNegativoLanzaExcepcion() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cuenta.depositar(new BigDecimal("-500.00"));
        });
        assertEquals("El importe debe ser mayor a cero", exception.getMessage());
    }

    @Test
    void testExtraerMontoValidoReduceSaldo() {
        cuenta.extraer(new BigDecimal("3000.00"));
        assertEquals(0, new BigDecimal("7000.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    void testExtraerSaldoInsuficienteLanzaExcepcion() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cuenta.extraer(new BigDecimal("15000.00"));
        });
        assertEquals("Saldo insuficiente", exception.getMessage());
    }

    @Test
    void testExtraerMontoNegativoLanzaExcepcion() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cuenta.extraer(new BigDecimal("-100.00"));
        });
        assertEquals("El importe debe ser mayor a cero", exception.getMessage());
    }
}