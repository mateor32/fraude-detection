package com.fraude.transaccion.controller;

import com.fraude.transaccion.model.Transaccion;
import com.fraude.transaccion.service.TransaccionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173", "http://127.0.0.1:5173"})
public class TransaccionController {

    private final TransaccionService service;

    public TransaccionController(TransaccionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> procesarTransaccion(@RequestBody Transaccion transaccion) {
        try {
            log.info("📥 Nueva solicitud de transacción recibida");
            Transaccion resultado = service.procesarTransaccion(transaccion);
            log.info("✅ Transacción procesada exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Validación fallida: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error al procesar transacción: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al procesar la transacción: " + e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable String numeroCuenta) {
        try {
            log.info("📋 Solicitud de historial para cuenta: {}", numeroCuenta);
            List<Transaccion> historial = service.obtenerHistorial(numeroCuenta);
            log.info("✅ Historial obtenido: {} transacciones", historial.size());
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            log.error("Error al obtener historial: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener el historial");
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Endpoints para administrador
    @GetMapping("/pendientes")
    public ResponseEntity<?> obtenerTransaccionesPendientes() {
        try {
            log.info("👨‍💼 Solicitud de transacciones pendientes (Admin)");
            List<Transaccion> pendientes = service.obtenerTransaccionesPendientes();
            log.info("✅ Transacciones pendientes obtenidas: {}", pendientes.size());
            return ResponseEntity.ok(pendientes);
        } catch (Exception e) {
            log.error("Error al obtener transacciones pendientes: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener transacciones pendientes");
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoTransaccion(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        try {
            log.info("👨‍💼 Solicitud de actualización de estado para transacción: {}", id);
            Integer nuevoEstado = body.get("estadoId");
            
            if (nuevoEstado == null || (nuevoEstado != 4 && nuevoEstado != 5 && nuevoEstado != 6)) {
                throw new IllegalArgumentException("Estado inválido. Debe ser 4 (PENDIENTE), 5 (APROBADA) o 6 (RECHAZADA)");
            }
            
            Transaccion actualizada = service.actualizarEstadoTransaccion(id, nuevoEstado);
            log.info("Estado de transacción actualizado: id={}, estado={}", id, nuevoEstado);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Validación fallida: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error al actualizar estado: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al actualizar el estado");
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}