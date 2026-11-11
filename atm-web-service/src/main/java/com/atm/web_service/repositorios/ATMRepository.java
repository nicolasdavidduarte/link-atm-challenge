package com.atm.web_service.repositorios;

import com.atm.web_service.entidades.Cuenta;
import com.atm.web_service.entidades.Tarjeta;
import com.atm.web_service.entidades.TarjetaCuenta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ATMRepository {

    private final Map<String, Tarjeta> tarjetas = new ConcurrentHashMap<>();
    private final Map<Long, Cuenta> cuentas =  new ConcurrentHashMap<>();
    private final Map<String, Cuenta> cuentasPorCbu = new ConcurrentHashMap<>();
    private final List<TarjetaCuenta> tarjetaCuentaList = new CopyOnWriteArrayList<>();

    public ATMRepository() {
        cargarDatos();
    }

    public Optional<Tarjeta> findByTarjeta(String nroTarjeta){
        return Optional.ofNullable(tarjetas.get(nroTarjeta));
    }

    public Optional<Cuenta> findByNumeroCuenta(Long nroCuenta){
        return Optional.ofNullable(cuentas.get(nroCuenta));
    }

    public Optional<Cuenta> findByCBU(String cbu){
        return Optional.ofNullable(cuentasPorCbu.get(cbu));
    }

    public Optional<TarjetaCuenta> findByTarjetaCuenta(String nroTarjeta, Long nroCuenta){
        return tarjetaCuentaList.stream().filter(tc -> tc.getTarjeta().equals(nroTarjeta) && tc.getCuenta().equals(nroCuenta)).findFirst();
    }

    private void cargarDatos(){
        Cuenta cuenta1 = Cuenta.crear(1001L, "0110001330000012345678", new BigDecimal("150000.50"));
        cuentas.put(cuenta1.getNumero(), cuenta1);
        cuentasPorCbu.put(cuenta1.getCbu(), cuenta1);

        Cuenta cuenta2 = Cuenta.crear(1002L, "0110001330000087654321", new BigDecimal("5400.00"));
        cuentas.put(cuenta2.getNumero(), cuenta2);
        cuentasPorCbu.put(cuenta2.getCbu(), cuenta2);

        Tarjeta tarjeta1 = Tarjeta.crear("1234567890123456", true);
        tarjetas.put(tarjeta1.getNumero(), tarjeta1);

        Tarjeta tarjeta2 = Tarjeta.crear("9876543210987654", false);
        tarjetas.put(tarjeta2.getNumero(), tarjeta2);

        TarjetaCuenta relacion1 = TarjetaCuenta.crear("1234567890123456", 1001L);
        tarjetaCuentaList.add(relacion1);

        TarjetaCuenta relacion2 = TarjetaCuenta.crear("9876543210987654", 1002L);
        tarjetaCuentaList.add(relacion2);
    }
}
