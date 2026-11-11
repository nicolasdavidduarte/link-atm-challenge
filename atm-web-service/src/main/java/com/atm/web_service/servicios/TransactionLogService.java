package com.atm.web_service.servicios;

import com.atm.web_service.entidades.TransactionLog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TransactionLogService {

    private static final Path logFilePath = Paths.get("log_transacciones.csv");
    private static final Object LOCK = new Object();

    public TransactionLogService() {}

    public void loggearTransacccion(TransactionLog transaccion) {
        synchronized (LOCK){
            try {
                boolean existeArchivo = Files.exists(logFilePath);

                try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true))) {

                    if (!existeArchivo) {
                        writer.println("Fecha,N° Transacción,Operación,N° Tarjeta,N° Cuenta,CBU,Importe,Operacion OK,Comentarios");
                    }

                    writer.println(transaccion.toCsvLine());
                }

            } catch (IOException e) {
                System.err.println("Error al escribir el log de la transacción: " + e.getMessage());
            }
        }
    }

}
