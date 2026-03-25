package com.fraude.usuario.repository;

import com.fraude.usuario.model.Usuario;
import com.fraude.usuario.model.UsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UsuarioId> {
}