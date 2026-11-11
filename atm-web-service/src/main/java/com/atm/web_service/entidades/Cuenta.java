package com.atm.web_service.entidades;

import java.math.BigDecimal;

public class Cuenta {
    private final Long numero;
    private final String cbu;
    private BigDecimal saldo;

    private Cuenta(Long numero, String cbu, BigDecimal saldo) {
        if (numero == null || numero <= 0) {
            throw new IllegalArgumentException("El número de cuenta debe ser válido");
        }
        if (cbu == null || cbu.isEmpty()) {
            throw new IllegalArgumentException("El CBU no puede estar vacío");
        }
        if (saldo == null || saldo.signum() < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }

        this.numero = numero;
        this.cbu = cbu;
        this.saldo = saldo;
    }

    public static Cuenta crear(Long numero, String cbu, BigDecimal saldo){
        return new Cuenta(numero, cbu, saldo);
    }

    public Long getNumero(){
        return this.numero;
    }

    public String getCbu(){
        return this.cbu;
    }

    public BigDecimal getSaldo(){
        return saldo;
    }

    public synchronized void extraer(BigDecimal importe){
        if(importe.signum() <= 0){
            throw new IllegalArgumentException("El importe debe ser mayor a cero");
        }

        if(saldo.compareTo(importe) < 0){
            throw new RuntimeException("Saldo insuficiente");
        }

        saldo = saldo.subtract(importe);
    }

    public synchronized void depositar(BigDecimal importe){
        if(importe.signum() <= 0){
            throw new IllegalArgumentException("El importe debe ser mayor a cero");
        }

        saldo = saldo.add(importe);
    }
}
