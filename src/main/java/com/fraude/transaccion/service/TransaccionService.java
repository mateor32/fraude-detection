package com.fraude.transaccion.service;

import com.fraude.cuenta.model.Cuenta;
import com.fraude.cuenta.repository.CuentaRepository;
import com.fraude.transaccion.model.Transaccion;
import com.fraude.transaccion.repository.TransaccionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
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
        try {
            log.info("Procesando transacción: origen={}, destino={}, monto={}",
                transaccion.getCuentaOrigenId(), 
                transaccion.getCuentaDestinoId(), 
                transaccion.getMonto());
            
            // Validaciones iniciales
            if (transaccion.getMonto() == null || transaccion.getMonto() <= 0) {
                log.error("Monto inválido: {}", transaccion.getMonto());
                throw new IllegalArgumentException("Monto debe ser mayor a 0");
            }
            
            if (transaccion.getCuentaOrigenId() == null || transaccion.getCuentaOrigenId().isEmpty()) {
                log.error("Cuenta origen vacía");
                throw new IllegalArgumentException("Cuenta origen es requerida");
            }
            
            if (transaccion.getCuentaDestinoId() == null || transaccion.getCuentaDestinoId().isEmpty()) {
                log.error("Cuenta destino vacía");
                throw new IllegalArgumentException("Cuenta destino es requerida");
            }
            
            // Verificar que las cuentas existan
            Cuenta origen = cuentaRepository.findById(transaccion.getCuentaOrigenId())
                    .orElseThrow(() -> {
                        log.error("Cuenta origen no existe: {}", transaccion.getCuentaOrigenId());
                        return new IllegalArgumentException("Cuenta origen no existe");
                    });

            Cuenta destino = cuentaRepository.findById(transaccion.getCuentaDestinoId())
                    .orElseThrow(() -> {
                        log.error("Cuenta destino no existe: {}", transaccion.getCuentaDestinoId());
                        return new IllegalArgumentException("Cuenta destino no existe");
                    });
            
            log.info("Cuentas validadas. Origen saldo: {}, Destino saldo: {}",
                origen.getSaldo(), destino.getSaldo());
            
            // Evaluar fraude
            Integer estadoId = fraudeService.evaluarFraude(transaccion);
            log.info("Estado de fraude evaluado: {}", estadoId);
            transaccion.setEstadoId(estadoId);

            // Si es aprobada, actualizar saldos
            if (estadoId == 5) { // APROBADA
                log.info("Transacción aprobada, actualizando saldos...");
                
                // Convertir monto a BigDecimal para operaciones financieras
                BigDecimal montoTransferencia = BigDecimal.valueOf(transaccion.getMonto());
                
                // Validación de saldo
                if (origen.getSaldo().compareTo(montoTransferencia) < 0) {
                    log.error("Saldo insuficiente: disponible={}, requerido={}",
                        origen.getSaldo(), montoTransferencia);
                    transaccion.setEstadoId(6); // RECHAZADA por saldo insuficiente
                } else {
                    // Actualizar saldos
                    origen.setSaldo(origen.getSaldo().subtract(montoTransferencia));
                    destino.setSaldo(destino.getSaldo().add(montoTransferencia));

                    cuentaRepository.save(origen);
                    cuentaRepository.save(destino);
                    
                    log.info("Saldos actualizados. Origen nuevo saldo: {}, Destino nuevo saldo: {}",
                        origen.getSaldo(), destino.getSaldo());
                }
            } else {
                log.info("ℹTransacción no aprobada (estado={}). Saldos no se actualizan.", estadoId);
            }

            // Asignar tipo y fecha
            transaccion.setTipoTransaccionId(1);
            transaccion.setFechaCreacion(LocalDateTime.now());

            // Guardar transacción
            Transaccion resultado = transaccionRepository.save(transaccion);
            log.info("Transacción guardada con ID: {}, Estado: {}", resultado.getId(), resultado.getEstadoId());
            
            return resultado;
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al procesar transacción: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar transacción: " + e.getMessage());
        }
    }

    public List<Transaccion> obtenerHistorial(String cuentaId) {
        try {
            log.info("📋 Obteniendo historial para cuenta: {}", cuentaId);
            
            List<Transaccion> enviadas = transaccionRepository.findByCuentaOrigenId(cuentaId);
            List<Transaccion> recibidas = transaccionRepository.findByCuentaDestinoId(cuentaId);

            List<Transaccion> historial = new ArrayList<>();
            historial.addAll(enviadas);
            historial.addAll(recibidas);
            
            historial.sort(Comparator.comparing(Transaccion::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())));

            log.info("Historial obtenido: {} transacciones", historial.size());
            return historial;
        } catch (Exception e) {
            log.error("Error al obtener historial: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // Métodos para administrador
    public List<Transaccion> obtenerTodasTransacciones() {
        try {
            List<Transaccion> transacciones = transaccionRepository.findAll();
            transacciones.sort(Comparator.comparing(Transaccion::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())));
            return transacciones;
        } catch (Exception e) {
            log.error("Error al obtener todas las transacciones: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Transaccion> obtenerTransaccionesPendientes() {
        try {
            log.info("Obteniendo todas las transacciones pendientes");
            List<Transaccion> pendientes = transaccionRepository.findByEstadoId(4);
            pendientes.sort(Comparator.comparing(Transaccion::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())));
            log.info("Transacciones pendientes obtenidas: {}", pendientes.size());
            return pendientes;
        } catch (Exception e) {
            log.error("Error al obtener transacciones pendientes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public Transaccion actualizarEstadoTransaccion(Integer id, Integer nuevoEstado) {
        try {
            log.info("Actualizando estado de transacción: id={}, nuevoEstado={}", id, nuevoEstado);
            
            Transaccion transaccion = transaccionRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Transacción no encontrada: {}", id);
                        return new IllegalArgumentException("Transacción no encontrada");
                    });

            Integer estadoAnterior = transaccion.getEstadoId();

            if (estadoAnterior == null || estadoAnterior != 4) {
                throw new IllegalArgumentException("Solo se pueden validar transacciones en estado PENDIENTE");
            }

            if (nuevoEstado == null || (nuevoEstado != 5 && nuevoEstado != 6)) {
                throw new IllegalArgumentException("Estado inválido para validación administrativa");
            }
            
            // Si cambia de PENDIENTE (4) a APROBADA (5), actualizar saldos
            if (estadoAnterior == 4 && nuevoEstado == 5) {
                log.info("Cambiando de PENDIENTE a APROBADA, actualizando saldos...");
                
                Cuenta origen = cuentaRepository.findById(transaccion.getCuentaOrigenId())
                        .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no existe"));

                Cuenta destino = cuentaRepository.findById(transaccion.getCuentaDestinoId())
                        .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no existe"));

                BigDecimal montoTransferencia = BigDecimal.valueOf(transaccion.getMonto());

                if (origen.getSaldo().compareTo(montoTransferencia) < 0) {
                    log.error("Saldo insuficiente al intentar aprobar");
                    throw new IllegalArgumentException("Saldo insuficiente en la cuenta origen");
                }

                origen.setSaldo(origen.getSaldo().subtract(montoTransferencia));
                destino.setSaldo(destino.getSaldo().add(montoTransferencia));

                cuentaRepository.save(origen);
                cuentaRepository.save(destino);
                
                log.info("Saldos actualizados correctamente");
            }

            transaccion.setEstadoId(nuevoEstado);
            Transaccion actualizada = transaccionRepository.save(transaccion);
            
            log.info("Transacción actualizada: id={}, estado anterior={}, estado nuevo={}",
                id, estadoAnterior, nuevoEstado);
            
            return actualizada;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar estado: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage());
        }
    }
}
