package com.atm.desktop;
import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

public class Main {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("Debe indicar una operación.");
            return;
        }

        AtmApiClient atm = new AtmApiClient("http://localhost:8080");

        String operacion = args[0];

        try {
            switch (operacion) {
                case "login":
                    System.out.println(gson.fromJson(atm.login(args[1]), String.class));
                    break;

                case "extraer":
                    System.out.println(gson.fromJson(atm.extraer(args[1], Long.parseLong(args[2]), new BigDecimal(args[3]), generarIdempotencyKey(args)), String.class));
                    break;

                case "depositar":
                    System.out.println(gson.fromJson(atm.depositar(
                            args[1], args[2], new BigDecimal(args[3]), generarIdempotencyKey(args)), String.class));
                    break;

                case "saldo":
                    System.out.println(gson.fromJson(atm.saldo(args[1], Long.parseLong(args[2])), String.class));
                    break;

                default:
                    System.out.println("Operación no reconocida");
            }
        } catch (Exception e) {
            System.out.println(gson.fromJson(e.getMessage(), String.class));
        }

    }

    private static String generarIdempotencyKey(String[] args){
        return (args.length > 4 && args[4] != null && !args[4].isEmpty())
                ? args[4]
                : UUID.randomUUID().toString();
    }
}
