package com.thequietchapter.repository;

import com.thequietchapter.model.RolUsuario;
import com.thequietchapter.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRol(RolUsuario rol);

    Page<Usuario> findByRol(RolUsuario rol, Pageable pageable);

    List<Usuario> findByRolOrderByIdAsc(RolUsuario rol);

    List<Usuario> findByIdIn(Collection<Long> ids);
}
