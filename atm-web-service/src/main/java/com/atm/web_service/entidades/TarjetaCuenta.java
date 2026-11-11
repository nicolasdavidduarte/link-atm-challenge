package com.atm.web_service.entidades;

import java.util.Objects;

public class TarjetaCuenta {
    private final String tarjeta;
    private final Long cuenta;

    private TarjetaCuenta(String tarjeta, Long cuenta) {
        this.tarjeta = tarjeta;
        this.cuenta = cuenta;
    }

    public static TarjetaCuenta crear(String tarjeta, Long cuenta){
        return new TarjetaCuenta(tarjeta, cuenta);
    }

    public String getTarjeta() {
        return this.tarjeta;
    }

    public Long getCuenta(){
        return this.cuenta;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TarjetaCuenta)) return false;

        TarjetaCuenta that = (TarjetaCuenta) o;

        return Objects.equals(tarjeta, that.tarjeta)
                && Objects.equals(cuenta, that.cuenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tarjeta, cuenta);
    }
}
