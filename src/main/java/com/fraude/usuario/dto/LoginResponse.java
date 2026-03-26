package com.fraude.usuario.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private boolean success;
    private String mensaje;
    private String email;
    private String nombre;
}

