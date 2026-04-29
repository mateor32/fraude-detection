package com.fraude.factura.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_factura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "num_documento", nullable = false)
    private String numDocumento;

    @Column(name = "tipo_documento_id", nullable = false)
    private Integer tipoDocumentoId;

    /**
     * Tipo de servicio: LUZ, AGUA, GAS, INTERNET, TELEFONO, TELEVISION, SEGUROS
     */
    @Column(name = "tipo_servicio", nullable = false)
    private String tipoServicio;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "referencia", nullable = false)
    private String referencia;

    @Column(name = "monto", nullable = false)
    private Double monto;

    /**
     * PENDIENTE, PAGADA, VENCIDA
     */
    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "tarjeta_id")
    private Integer tarjetaId;

    /** ID del PaymentIntent en Stripe */
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
