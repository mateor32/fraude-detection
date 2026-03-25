package com.fraude.transaccion.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_transaccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal monto;

    private String descripcion;

    @Column(name = "cuenta_origen_id")
    private String cuentaOrigenId;

    @Column(name = "cuenta_destino_id")
    private String cuentaDestinoId;

    @Column(name = "estado_id")
    private Integer estadoId;

    @Column(name = "tipo_transaccion_id")
    private Integer tipoTransaccionId;

    @Column(name = "fecha")
    private LocalDateTime fechaCreacion;
}