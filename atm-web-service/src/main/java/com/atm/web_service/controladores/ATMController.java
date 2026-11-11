package com.atm.web_service.controladores;

import com.atm.web_service.servicios.ATMService;

import java.math.BigDecimal;

public class ATMController {

    private final ATMService atmService;

    public ATMController(ATMService atmService) {
        this.atmService = atmService;
    }

    public String login(String nroTarjeta){
        return atmService.login(nroTarjeta);
    }

    public String extraer(String nroTarjeta, Long cuenta, BigDecimal importe){
        return atmService.extraer(nroTarjeta, cuenta, importe);
    }

    public String depositar(String nroTarjeta, String cbu, BigDecimal importe){
        return atmService.depositar(nroTarjeta, cbu, importe);
    }

    public String consultarSaldo(String nroTarjeta, Long cuenta){
        return atmService.consultarSaldo(nroTarjeta, cuenta);
    }
}
