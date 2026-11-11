package com.atm.web_service.entidades;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionLog {
    private final LocalDateTime fecha;
    private final String nroTransaccion;
    private final String nroTarjeta;
    private final Long nroCuenta;
    private final String cbu;
    private final String tipo;
    private final BigDecimal importe;
    private final boolean operacionOK;
    private final String comentarios;

    private TransactionLog(LocalDateTime fecha, String nroTransaccion, String nroTarjeta, Long nroCuenta, String cbu, String tipo, BigDecimal importe, boolean operacionOK, String comentarios) {
        this.fecha = fecha;
        this.nroTarjeta = nroTarjeta;
        this.nroTransaccion = nroTransaccion;
        this.nroCuenta = nroCuenta;
        this.cbu = cbu;
        this.tipo = tipo;
        this.importe = importe;
        this.operacionOK = operacionOK;
        this.comentarios = comentarios;
    }

    public static TransactionLog crear(LocalDateTime fecha, String nroTransaccion, String nroTarjeta, Long nroCuenta, String cbu, String tipo, BigDecimal importe, boolean operacionOK, String comentarios){
        return new TransactionLog(fecha, nroTransaccion, nroTarjeta, nroCuenta, cbu, tipo, importe, operacionOK, comentarios);
    }

    public String toCsvLine() {
        String tarjetaVal = (nroTarjeta != null) ? nroTarjeta : "N/A";
        String cuentaVal = (nroCuenta != null) ? nroCuenta.toString() : "N/A";
        String cbuVal = (cbu != null) ? cbu  : "N/A";
        String importeVal = (importe != null) ? String.format("%.2f", importe) : "N/A";

        return String.format("%s,%s,%s,%s,%s,%s,%s,%b,%s",
                fecha,
                nroTransaccion,
                tipo,
                tarjetaVal,
                cuentaVal,
                cbuVal,
                importeVal,
                operacionOK,
                comentarios
        );
    }
}