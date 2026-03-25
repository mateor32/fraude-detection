package com.fraude.transaccion.service;

import com.fraude.cuenta.model.Cuenta;
import com.fraude.cuenta.repository.CuentaRepository;
import com.fraude.transaccion.model.Transaccion;
import com.fraude.transaccion.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransaccionService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    public TransaccionService(CuentaRepository cuentaRepository,
                              TransaccionRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional
    public Transaccion transferir(String origenId, String destinoId, BigDecimal monto){
        System.out.println("Origen: " + origenId);
        System.out.println("Destino: " + destinoId);
        Cuenta origen = cuentaRepository.findById(origenId)
                .orElseThrow(() -> new RuntimeException("Cuenta origen no existe"));

        Cuenta destino = cuentaRepository.findById(destinoId)
                .orElseThrow(() -> new RuntimeException("Cuenta destino no existe"));

        // 🔥 VALIDACIÓN
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        // 💰 ACTUALIZAR SALDOS
        origen.setSaldo(origen.getSaldo().subtract(monto));
        destino.setSaldo(destino.getSaldo().add(monto));

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        // 🧾 CREAR TRANSACCIÓN
        Transaccion t = Transaccion.builder()
                .monto(monto)
                .cuentaOrigenId(origenId)
                .cuentaDestinoId(destinoId)
                .estadoId(1)
                .tipoTransaccionId(1)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return transaccionRepository.save(t);
    }

    public List<Transaccion> obtenerHistorial(String numeroCuenta) {

        List<Transaccion> enviadas = transaccionRepository
                .findByCuentaOrigenId(numeroCuenta);

        List<Transaccion> recibidas = transaccionRepository
                .findByCuentaDestinoId(numeroCuenta);

        List<Transaccion> historial = new ArrayList<>();
        historial.addAll(enviadas);
        historial.addAll(recibidas);

        return historial;
    }
}