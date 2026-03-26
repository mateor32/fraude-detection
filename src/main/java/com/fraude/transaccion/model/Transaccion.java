package com.fraude.transaccion.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "monto")
    private Double monto;

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