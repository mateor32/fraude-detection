package com.fraude.tarjeta.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_tarjeta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "num_documento", nullable = false)
    private String numDocumento;

    @Column(name = "tipo_documento_id", nullable = false)
    private Integer tipoDocumentoId;

    /** CREDITO o DEBITO */
    @Column(name = "tipo_tarjeta", nullable = false)
    private String tipoTarjeta;

    /** Últimos 4 dígitos de la tarjeta */
    @Column(name = "ultimos_cuatro")
    private String ultimosCuatro;

    @Column(name = "nombre_titular", nullable = false)
    private String nombreTitular;

    /** MM/YY */
    @Column(name = "fecha_expiracion")
    private String fechaExpiracion;

    /** Marca: VISA, MASTERCARD, AMEX */
    @Column(name = "marca")
    private String marca;

    /** ID del customer en Stripe */
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    /** ID del PaymentMethod en Stripe */
    @Column(name = "stripe_payment_method_id")
    private String stripePaymentMethodId;

    /**
     * 1 = ACTIVA, 2 = PENDIENTE (esperando aprobación admin),
     * 3 = ELIMINADA, 4 = RECHAZADA
     */
    @Column(name = "estado_id")
    private Integer estadoId;

    /** Límite de crédito asignado por el admin (solo tarjetas CREDITO) */
    @Column(name = "limite_credito")
    private Double limiteCredito;

    /** Crédito disponible actual (se reduce al pagar con tarjeta de crédito) */
    @Column(name = "credito_disponible")
    private Double creditoDisponible;

    /** Saldo cargado en la tarjeta (solo tarjetas DEBITO) */
    @Column(name = "saldo_tarjeta")
    private Double saldoTarjeta;

    /** Motivo del rechazo (opcional) */
    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
