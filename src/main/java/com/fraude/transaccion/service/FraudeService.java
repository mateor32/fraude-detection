package com.fraude.transaccion.service;

import com.fraude.transaccion.model.Transaccion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FraudeService {

    public Integer evaluarFraude(Transaccion transaccion) {
        try {
            log.info("Evaluando fraude para transacción: monto={}", transaccion.getMonto());
            
            // Validar que monto no sea nulo
            if (transaccion.getMonto() == null) {
                log.warn("⚠Monto es nulo");
                return 6; // RECHAZADA
            }
            
            // Convertir a double para comparación
            double monto = transaccion.getMonto();
            
            if (monto > 5000000) {
                log.info("Monto sospechoso (> $5,000,000): $" + monto);
                return 4; // PENDIENTE - Requiere revisión
            } else {
                log.info("Monto bajo (≤ $5,000,000): $" + monto);
                return 5; // APROBADA
            }
        } catch (Exception e) {
            log.error("Error al evaluar fraude", e);
            return 6; // RECHAZADA por error
        }
    }
}

