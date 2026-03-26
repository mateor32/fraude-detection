package com.fraude.usuario.service;

import com.fraude.usuario.dto.LoginRequest;
import com.fraude.usuario.dto.LoginResponse;
import com.fraude.usuario.model.Usuario;
import com.fraude.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> getAllUsuarios() {
        return repository.findAll();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Usuario usuario = repository.findByNumDocumento(loginRequest.getNumDocumento())
                .orElse(null);

        if (usuario == null) {
            return LoginResponse.builder()
                    .success(false)
                    .mensaje("Usuario no encontrado")
                    .build();
        }

        // Comparar password (por ahora, comparación directa)
        if (!usuario.getPasswordHash().equals(loginRequest.getPassword())) {
            return LoginResponse.builder()
                    .success(false)
                    .mensaje("Contraseña incorrecta")
                    .build();
        }

        // Login exitoso
        return LoginResponse.builder()
                .success(true)
                .mensaje("Login exitoso")
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .build();
    }
}