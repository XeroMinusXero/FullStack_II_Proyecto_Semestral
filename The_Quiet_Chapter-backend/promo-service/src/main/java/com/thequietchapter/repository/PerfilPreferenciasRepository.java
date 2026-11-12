package com.thequietchapter.repository;

import com.thequietchapter.model.PerfilPreferencias;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerfilPreferenciasRepository extends JpaRepository<PerfilPreferencias, Long> {
    Optional<PerfilPreferencias> findByClienteId(Long clienteId);
}
