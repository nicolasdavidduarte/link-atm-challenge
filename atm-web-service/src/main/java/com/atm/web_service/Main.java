package com.atm.web_service;

import com.atm.web_service.controladores.ATMController;
import com.atm.web_service.repositorios.ATMRepository;
import com.atm.web_service.servicios.ATMService;
import com.atm.web_service.servicios.IdempotencyService;
import com.atm.web_service.servicios.TransactionLogService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        ATMRepository atmRepository = new ATMRepository();

        TransactionLogService transactionLogService = new TransactionLogService();

        ATMService atmService = new ATMService(atmRepository, transactionLogService);

        ATMController atmController = new ATMController(atmService);

        IdempotencyService idempotencyService = new IdempotencyService();

        AtmHttpServer atmHttpServer = new AtmHttpServer(atmController, idempotencyService);

        atmHttpServer.start();

        System.out.println("ATM Web Service corriendo en puerto 8080");
    }
}
