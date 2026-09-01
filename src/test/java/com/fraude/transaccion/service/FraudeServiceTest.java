package com.fraude.transaccion.service;

import com.fraude.transaccion.model.Transaccion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure unit tests for {@link FraudeService#evaluarFraude}. No Spring context,
 * no database — just the fraud-scoring rules.
 */
class FraudeServiceTest {

    private final FraudeService fraudeService = new FraudeService();

    private Transaccion conMonto(Double monto) {
        Transaccion t = new Transaccion();
        t.setMonto(monto);
        return t;
    }

    @Test
    void montoNuloSeRechaza() {
        assertEquals("RECHAZADA", fraudeService.evaluarFraude(conMonto(null)));
    }

    @Test
    void montoBajoSeAprueba() {
        assertEquals("APROBADA", fraudeService.evaluarFraude(conMonto(1_000_000.0)));
    }

    @Test
    void montoJustoEnElLimiteSeAprueba() {
        assertEquals("APROBADA", fraudeService.evaluarFraude(conMonto(5_000_000.0)));
    }

    @Test
    void montoSobreElLimiteQuedaPendiente() {
        assertEquals("PENDIENTE", fraudeService.evaluarFraude(conMonto(5_000_000.01)));
    }

    @Test
    void montoMuyAltoQuedaPendiente() {
        assertEquals("PENDIENTE", fraudeService.evaluarFraude(conMonto(50_000_000.0)));
    }
}
