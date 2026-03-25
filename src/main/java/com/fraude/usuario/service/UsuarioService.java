package com.fraude.usuario.service;

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
}