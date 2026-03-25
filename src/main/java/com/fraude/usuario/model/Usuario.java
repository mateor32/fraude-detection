package com.fraude.usuario.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @EmbeddedId
    private UsuarioId id;

    @Column(name = "rol_id")
    private Integer rolId;

    private String nombre;

    private String apellido;

    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "estado_id")
    private Integer estadoId;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}