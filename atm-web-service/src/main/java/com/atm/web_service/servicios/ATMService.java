package com.atm.web_service.servicios;

import com.atm.web_service.entidades.Cuenta;
import com.atm.web_service.entidades.Tarjeta;
import com.atm.web_service.entidades.TransactionLog;
import com.atm.web_service.repositorios.ATMRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ATMService {

    private final ATMRepository atmRepository;
    private final TransactionLogService transactionLogService;

    public ATMService(ATMRepository atmRepository, TransactionLogService transactionLogService) {
        this.atmRepository = atmRepository;
        this.transactionLogService = transactionLogService;
    }

    public String login(String nroTarjeta) {
        boolean operacionOK = false;
        String txId = "TX-" + System.currentTimeMillis();
        String comentarios = "";

        try {
            Tarjeta tarjeta = atmRepository.findByTarjeta(nroTarjeta).orElseThrow(() -> new RuntimeException("Ingreso no exitoso"));

            if (tarjeta.getActiva()) {
                operacionOK = true;
                return "Ingreso exitoso";
            } else {
                return "Ingreso no exitoso";
            }
        }catch (Exception e){
            comentarios = e.getMessage();
            throw e;
        }finally {
            TransactionLog tx = TransactionLog.crear(
                    LocalDateTime.now(),
                    txId,
                    nroTarjeta,
                    null,
                    null,
                    "LOGIN",
                    null,
                    operacionOK,
                    comentarios
            );

            transactionLogService.loggearTransacccion(tx);
        }

    }

    public String extraer(String nroTarjeta, Long nroCuenta, BigDecimal importe) {
        boolean operacionOK = false;
        String txId = "TX-" + System.currentTimeMillis();
        String comentarios = "";

        try {
            Tarjeta tarjeta = atmRepository.findByTarjeta(nroTarjeta).orElseThrow(() -> new RuntimeException("La tarjeta no existe"));
            Cuenta cuenta = atmRepository.findByNumeroCuenta(nroCuenta).orElseThrow(() -> new RuntimeException("La cuenta no existe con ese número"));

            atmRepository.findByTarjetaCuenta(nroTarjeta, nroCuenta).orElseThrow(() -> new RuntimeException("No puede extraer de la cuenta con esta tarjeta"));

            if(!tarjeta.getActiva()){
                throw new RuntimeException("No puede extraer dinero con una tarjeta inactiva");
            }

            cuenta.extraer(importe);

            operacionOK = true;

            return "Retire su dinero";
        } catch (Exception e) {
            comentarios = e.getMessage();
            throw e;
        } finally {
            TransactionLog tx = TransactionLog.crear(
                    LocalDateTime.now(),
                    txId,
                    nroTarjeta,
                    nroCuenta,
                    null,
                    "EXTRACCIÓN",
                    importe,
                    operacionOK,
                    comentarios
            );

            transactionLogService.loggearTransacccion(tx);
        }

    }

    public String depositar(String nroTarjeta, String cbu, BigDecimal importe) {
        boolean operacionOK = false;
        String txId = "TX-" + System.currentTimeMillis();
        String comentarios = "";

        try{
            Tarjeta tarjeta = atmRepository.findByTarjeta(nroTarjeta).orElseThrow(() -> new RuntimeException("Tarjeta no encontrada en el sistema"));

            if(!tarjeta.getActiva()){
                throw new RuntimeException("Tarjeta inactiva");
            }

            Cuenta cuenta = atmRepository.findByCBU(cbu).orElseThrow(() -> new RuntimeException("La cuenta no existe con ese CBU"));

            cuenta.depositar(importe);

            operacionOK = true;

            return "Depósito exitoso";
        } catch (Exception e) {
            comentarios = e.getMessage();
            throw e;
        } finally {
            TransactionLog tx = TransactionLog.crear(
                    LocalDateTime.now(),
                    txId,
                    nroTarjeta,
                    null,
                    cbu,
                    "DEPÓSITO",
                    importe,
                    operacionOK,
                    comentarios
            );

            transactionLogService.loggearTransacccion(tx);
        }

    }

    public String consultarSaldo(String nroTarjeta, Long nroCuenta){
        boolean operacionOK = false;
        String txId = "TX-" + System.currentTimeMillis();
        String comentarios = "";

        try{
            atmRepository.findByTarjetaCuenta(nroTarjeta, nroCuenta).orElseThrow(() -> new RuntimeException("No puede leer el saldo de la cuenta con esta tarjeta"));

            Tarjeta tarjeta = atmRepository.findByTarjeta(nroTarjeta).orElseThrow(() -> new RuntimeException("Tarjeta no encontrada en el sistema"));

            if(!tarjeta.getActiva()){
                throw new RuntimeException("Tarjeta inactiva");
            }

            Cuenta cuenta = atmRepository.findByNumeroCuenta(nroCuenta).orElseThrow(() -> new RuntimeException("La cuenta no existe con ese número"));

            java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "AR"));
            symbols.setDecimalSeparator(',');
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00", symbols);

            String saldo = "Su saldo es $" + df.format(cuenta.getSaldo());

            operacionOK = true;

            return saldo;
        } catch (Exception e) {
            comentarios = e.getMessage();
            throw e;
        } finally {
            TransactionLog tx = TransactionLog.crear(
                    LocalDateTime.now(),
                    txId,
                    nroTarjeta,
                    nroCuenta,
                    null,
                    "CONSULTA_SALDO",
                    null,
                    operacionOK,
                    comentarios
            );

            transactionLogService.loggearTransacccion(tx);
        }
    }
}