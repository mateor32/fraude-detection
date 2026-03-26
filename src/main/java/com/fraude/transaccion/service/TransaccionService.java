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

    private final TransaccionRepository transaccionRepository;
    private final FraudeService fraudeService;
    private final CuentaRepository cuentaRepository;

    public TransaccionService(TransaccionRepository transaccionRepository, FraudeService fraudeService, CuentaRepository cuentaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.fraudeService = fraudeService;
        this.cuentaRepository = cuentaRepository;
    }

    @Transactional
    public Transaccion procesarTransaccion(Transaccion transaccion) {
        // Evaluar fraude
        Integer estadoId = fraudeService.evaluarFraude(transaccion);
        transaccion.setEstadoId(estadoId);

        // Si es aprobada, actualizar saldos
        if (estadoId == 5) { // APROBADA
            Cuenta origen = cuentaRepository.findById(transaccion.getCuentaOrigenId())
                    .orElseThrow(() -> new RuntimeException("Cuenta origen no existe"));

            Cuenta destino = cuentaRepository.findById(transaccion.getCuentaDestinoId())
                    .orElseThrow(() -> new RuntimeException("Cuenta destino no existe"));

            // Validación de saldo
            if (origen.getSaldo().compareTo(BigDecimal.valueOf(transaccion.getMonto())) <= 0) {
                throw new RuntimeException("Saldo insuficiente");
            }

            // Actualizar saldos
            origen.setSaldo(origen.getSaldo().subtract(BigDecimal.valueOf(transaccion.getMonto())));
            destino.setSaldo(destino.getSaldo().add(BigDecimal.valueOf(transaccion.getMonto())));

            cuentaRepository.save(origen);
            cuentaRepository.save(destino);
        }

        // Asignar tipo y fecha
        transaccion.setTipoTransaccionId(1);
        transaccion.setFechaCreacion(LocalDateTime.now());

        return transaccionRepository.save(transaccion);
    }

    public List<Transaccion> obtenerHistorial(String cuentaId) {
        List<Transaccion> enviadas = transaccionRepository.findByCuentaOrigenId(cuentaId);
        List<Transaccion> recibidas = transaccionRepository.findByCuentaDestinoId(cuentaId);

        List<Transaccion> historial = new ArrayList<>();
        historial.addAll(enviadas);
        historial.addAll(recibidas);

        return historial;
    }
}