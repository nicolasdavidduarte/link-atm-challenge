package com.atm.web_service.servicios;

import com.atm.web_service.entidades.Cuenta;
import com.atm.web_service.entidades.Tarjeta;
import com.atm.web_service.entidades.TarjetaCuenta;
import com.atm.web_service.repositorios.ATMRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMServiceTest {

    private ATMRepository atmRepositoryMock;
    private ATMService atmService;

    @BeforeEach
    void setUp() {
        atmRepositoryMock = mock(ATMRepository.class);
        TransactionLogService transactionLogServiceMock = mock(TransactionLogService.class);
        atmService = new ATMService(atmRepositoryMock, transactionLogServiceMock);
    }

    @Test
    void testLoginExitoso() {
        Tarjeta tarjeta = Tarjeta.crear("1234567890123456", true);

        when(atmRepositoryMock.findByTarjeta("1234567890123456")).thenReturn(Optional.of(tarjeta));

        String resultado = atmService.login("1234567890123456");

        assertEquals("Ingreso exitoso", resultado);
    }

    @Test
    void testLoginTarjetaInactiva() {
        Tarjeta tarjeta = Tarjeta.crear("1234567890123456", false);

        when(atmRepositoryMock.findByTarjeta("1234567890123456")).thenReturn(Optional.of(tarjeta));

        String resultado = atmService.login("1234567890123456");

        assertEquals("Ingreso no exitoso", resultado);
    }

    @Test
    void testLoginTarjetaNoEncontradaLanzaExcepcion() {
        when(atmRepositoryMock.findByTarjeta("9999")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            atmService.login("9999");
        });

        assertEquals("Ingreso no exitoso", exception.getMessage());
    }

    @Test
    void testExtraerExitoso() {
        Tarjeta tarjeta = Tarjeta.crear("1234567890123456", true);
        Cuenta cuenta = Cuenta.crear(1001L, "001343431431431431", new BigDecimal("10000.00"));
        TarjetaCuenta tarjetaCuenta = TarjetaCuenta.crear("1234567890123456", 1001L);

        when(atmRepositoryMock.findByTarjeta("1234567890123456"))
                .thenReturn(Optional.of(tarjeta));
        when(atmRepositoryMock.findByTarjetaCuenta("1234567890123456", 1001L))
                .thenReturn(Optional.of(tarjetaCuenta));
        when(atmRepositoryMock.findByNumeroCuenta(1001L))
                .thenReturn(Optional.of(cuenta));

        String resultado = atmService.extraer("1234567890123456", 1001L, new BigDecimal("3000.00"));

        assertEquals("Retire su dinero", resultado);
        assertEquals(0, new BigDecimal("7000.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    void testExtraerSaldoInsuficienteLanzaExcepcion() {
        Tarjeta tarjeta = Tarjeta.crear("1234567890123456", true);
        Cuenta cuenta = Cuenta.crear(1001L, "121343431431431431", new BigDecimal("1000.00"));
        TarjetaCuenta tarjetaCuenta = TarjetaCuenta.crear("121343431431431431", 1001L);

        when(atmRepositoryMock.findByTarjeta("1234567890123456"))
                .thenReturn(Optional.of(tarjeta));
        when(atmRepositoryMock.findByTarjetaCuenta("1234567890123456", 1001L))
                .thenReturn(Optional.of(tarjetaCuenta));
        when(atmRepositoryMock.findByNumeroCuenta(1001L))
                .thenReturn(Optional.of(cuenta));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            atmService.extraer("1234567890123456", 1001L, new BigDecimal("5000.00"));
        });

        assertEquals("Saldo insuficiente", exception.getMessage());
    }

    @Test
    void testDepositarExitoso() {
        Tarjeta tarjeta = Tarjeta.crear("1234567890123456", true);

        Cuenta cuenta = Cuenta.crear(1001L, "0110001330000012345678", new BigDecimal("5000.00"));

        when(atmRepositoryMock.findByTarjeta("1234567890123456")).thenReturn(Optional.of(tarjeta));
        when(atmRepositoryMock.findByCBU("0110001330000012345678")).thenReturn(Optional.of(cuenta));

        String resultado = atmService.depositar("1234567890123456", "0110001330000012345678", new BigDecimal("2500.00"));

        assertEquals("Depósito exitoso", resultado);
        assertEquals(0, new BigDecimal("7500.00").compareTo(cuenta.getSaldo()));
    }

    @Test
    void testConsultarSaldoExitoso() {
        Tarjeta tarjeta = Tarjeta.crear("1234567890123456", true);
        Cuenta cuenta = Cuenta.crear(1001L, "0110001330000012345678", new BigDecimal("12345.50"));
        TarjetaCuenta tarjetaCuenta = TarjetaCuenta.crear("121343431431431431", 1001L);

        when(atmRepositoryMock.findByTarjeta("1234567890123456"))
                .thenReturn(Optional.of(tarjeta));
        when(atmRepositoryMock.findByTarjetaCuenta("1234567890123456", 1001L))
                .thenReturn(Optional.of(tarjetaCuenta));
        when(atmRepositoryMock.findByNumeroCuenta(1001L))
                .thenReturn(Optional.of(cuenta));

        String resultado = atmService.consultarSaldo("1234567890123456", 1001L);

        assertEquals("Su saldo es $12345,50", resultado);
    }
}