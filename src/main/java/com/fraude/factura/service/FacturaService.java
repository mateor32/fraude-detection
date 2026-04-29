package com.fraude.factura.service;

import com.fraude.cuenta.model.Cuenta;
import com.fraude.cuenta.repository.CuentaRepository;
import com.fraude.factura.model.Factura;
import com.fraude.factura.repository.FacturaRepository;
import com.fraude.tarjeta.model.Tarjeta;
import com.fraude.tarjeta.repository.TarjetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final TarjetaRepository tarjetaRepository;
    private final CuentaRepository cuentaRepository;

    public List<Factura> generarFacturasPrueba(String numDocumento, Integer tipoDoc) {
        String[] servicios = { "LUZ", "AGUA", "INTERNET", "GAS", "TELEFONO" };
        String[] descripciones = {
                "Electricidad - EPM Mes actual",
                "Acueducto y alcantarillado - Aguas Nacionales",
                "Plan Hogar 200 Mbps - Internet Plus",
                "Gas natural domiciliario - GasNatural",
                "Plan movil 10GB - TeleCel"
        };
        double[] montos = { 85000, 42000, 75000, 38000, 59900 };

        for (int i = 0; i < servicios.length; i++) {
            final String servicioActual = servicios[i];
            final String descripcionActual = descripciones[i];
            final double montoActual = montos[i];
            final int diasExtra = i;
            List<Factura> existentes = facturaRepository.findByNumDocumentoAndEstado(numDocumento, "PENDIENTE");
            boolean yaExiste = existentes.stream().anyMatch(f -> f.getTipoServicio().equals(servicioActual));
            if (!yaExiste) {
                Factura factura = Factura.builder()
                        .numDocumento(numDocumento)
                        .tipoDocumentoId(tipoDoc)
                        .tipoServicio(servicioActual)
                        .descripcion(descripcionActual)
                        .referencia("REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .monto(montoActual)
                        .estado("PENDIENTE")
                        .fechaVencimiento(LocalDateTime.now().plusDays(15 + diasExtra * 3))
                        .fechaCreacion(LocalDateTime.now())
                        .build();
                facturaRepository.save(factura);
            }
        }
        return facturaRepository.findByNumDocumentoOrderByFechaVencimientoDesc(numDocumento);
    }

    public List<Factura> obtenerFacturas(String numDocumento) {
        return facturaRepository.findByNumDocumentoOrderByFechaVencimientoDesc(numDocumento);
    }

    public Factura pagarFactura(Integer facturaId, String numDocumento,
            Integer tarjetaId, String numeroCuenta) {

        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        if (!factura.getNumDocumento().equals(numDocumento)) {
            throw new RuntimeException("No tienes permiso para pagar esta factura");
        }
        if ("PAGADA".equals(factura.getEstado())) {
            throw new RuntimeException("Esta factura ya fue pagada");
        }

        if (tarjetaId != null) {
            Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId)
                    .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

            if (!tarjeta.getNumDocumento().equals(numDocumento)) {
                throw new RuntimeException("La tarjeta no pertenece al usuario");
            }
            if (tarjeta.getEstadoId() != 1) {
                throw new RuntimeException("La tarjeta no esta activa");
            }

            double monto = factura.getMonto();

            if ("CREDITO".equals(tarjeta.getTipoTarjeta())) {
                double disponible = tarjeta.getCreditoDisponible() != null ? tarjeta.getCreditoDisponible() : 0.0;
                if (disponible < monto) {
                    throw new RuntimeException(
                            "Credito disponible insuficiente. Disponible: $" + disponible + " | Factura: $" + monto);
                }
                tarjeta.setCreditoDisponible(disponible - monto);
                log.info("Pago con tarjeta credito. Credito restante: {}", tarjeta.getCreditoDisponible());
            } else {
                double saldo = tarjeta.getSaldoTarjeta() != null ? tarjeta.getSaldoTarjeta() : 0.0;
                if (saldo < monto) {
                    throw new RuntimeException(
                            "Saldo de tarjeta debito insuficiente. Saldo: $" + saldo + " | Factura: $" + monto);
                }
                tarjeta.setSaldoTarjeta(saldo - monto);
                log.info("Pago con tarjeta debito. Saldo restante: {}", tarjeta.getSaldoTarjeta());
            }

            tarjetaRepository.save(tarjeta);
            factura.setTarjetaId(tarjetaId);

        } else if (numeroCuenta != null) {
            Cuenta cuenta = cuentaRepository.findById(numeroCuenta)
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            if (!cuenta.getNumDocumento().equals(numDocumento)) {
                throw new RuntimeException("La cuenta no pertenece al usuario");
            }
            if (cuenta.getSaldo().compareTo(BigDecimal.valueOf(factura.getMonto())) < 0) {
                throw new RuntimeException(
                        "Saldo insuficiente. Saldo: $" + cuenta.getSaldo() + " | Factura: $" + factura.getMonto());
            }

            cuenta.setSaldo(cuenta.getSaldo().subtract(BigDecimal.valueOf(factura.getMonto())));
            cuentaRepository.save(cuenta);
            log.info("Factura pagada con saldo de cuenta. Nuevo saldo: {}", cuenta.getSaldo());

        } else {
            throw new RuntimeException("Debes indicar una tarjeta o una cuenta para pagar");
        }

        factura.setEstado("PAGADA");
        factura.setFechaPago(LocalDateTime.now());
        facturaRepository.save(factura);

        log.info("Factura {} pagada exitosamente", facturaId);
        return factura;
    }
}