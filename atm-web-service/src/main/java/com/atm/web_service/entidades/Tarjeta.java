package com.atm.web_service.entidades;

public class Tarjeta {
    private final String numero;
    private boolean activa;

    private Tarjeta(String numero, boolean activa) {
        if(numero.length() > 16){
            throw new RuntimeException("El número de tarjeta no puede exceder los 16 dígitos");
        }

        this.numero = numero;
        this.activa = activa;
    }

    public static Tarjeta crear(String numero, boolean activa){
        return new Tarjeta(numero, activa) ;
    }

    public String getNumero() {
        return this.numero;
    }

    public boolean getActiva(){
        return this.activa;
    }

    public void activar(){
        this.activa = true;
    }

    public void desactivar(){
        this.activa = false;
    }

}