package com.fraude.usuario.service;

import com.fraude.usuario.dto.LoginRequest;
import com.fraude.usuario.dto.LoginResponse;
import com.fraude.usuario.model.Usuario;
import com.fraude.usuario.repository.UsuarioRepository;
import com.fraude.cuenta.model.Cuenta;
import com.fraude.cuenta.repository.CuentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final CuentaRepository cuentaRepository;

    public UsuarioService(UsuarioRepository repository, CuentaRepository cuentaRepository) {
        this.repository = repository;
        this.cuentaRepository = cuentaRepository;
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

        // Obtener información de la cuenta
        Cuenta cuenta = cuentaRepository.findByNumDocumento(loginRequest.getNumDocumento())
                .orElse(null);



        // Login exitoso
        return LoginResponse.builder()
                .success(true)
                .mensaje("Login exitoso")
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .saldo(cuenta != null ? cuenta.getSaldo() : null)
                .numeroCuenta(cuenta != null ? cuenta.getNumeroCuenta() : loginRequest.getNumDocumento())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                .build();
    }

    public boolean esAdministrador(String numDocumento) {
        if (numDocumento == null || numDocumento.isBlank()) {
            return false;
        }

        return repository.findByNumDocumento(numDocumento.trim())
                .map(Usuario::getRol)
                .map(rol -> rol != null ? rol.getNombre() : null)
                .map(this::esRolAdmin)
                .orElse(false);
    }

    private boolean esRolAdmin(String rolNombre) {
        if (rolNombre == null || rolNombre.isBlank()) {
            return false;
        }

        String normalizado = rolNombre.trim().toUpperCase();
        return normalizado.equals("ADMIN") || normalizado.equals("ADMINISTRADOR");
    }
}