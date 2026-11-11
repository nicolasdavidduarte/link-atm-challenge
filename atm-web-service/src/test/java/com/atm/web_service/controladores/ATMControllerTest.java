package com.atm.web_service.controladores;

import com.atm.web_service.servicios.ATMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMControllerTest {

    private ATMService atmServiceMock;
    private ATMController atmController;

    @BeforeEach
    void setUp() {
        atmServiceMock = mock(ATMService.class);
        atmController = new ATMController(atmServiceMock);
    }

    @Test
    void testControllerLogin() {
        when(atmServiceMock.login("1234")).thenReturn("Ingreso exitoso");

        String response = atmController.login("1234");

        assertEquals("Ingreso exitoso", response);
        verify(atmServiceMock, times(1)).login("1234");
    }

    @Test
    void testControllerExtraer() {
        BigDecimal importe = new BigDecimal("1000.00");
        when(atmServiceMock.extraer("1234", 1001L, importe)).thenReturn("Retire su dinero");

        String response = atmController.extraer("1234", 1001L, importe);

        assertEquals("Retire su dinero", response);
        verify(atmServiceMock, times(1)).extraer("1234", 1001L, importe);
    }

    @Test
    void testControllerDepositar() {
        BigDecimal importe = new BigDecimal("500.00");
        when(atmServiceMock.depositar("1234", "CBU123", importe)).thenReturn("Depósito exitoso");

        String response = atmController.depositar("1234", "CBU123", importe);

        assertEquals("Depósito exitoso", response);
        verify(atmServiceMock, times(1)).depositar("1234", "CBU123", importe);
    }

    @Test
    void testControllerConsultarSaldo() {
        when(atmServiceMock.consultarSaldo("1234", 1001L)).thenReturn("Su saldo es $500.00");

        String response = atmController.consultarSaldo("1234", 1001L);

        assertEquals("Su saldo es $500.00", response);
        verify(atmServiceMock, times(1)).consultarSaldo("1234", 1001L);
    }
}