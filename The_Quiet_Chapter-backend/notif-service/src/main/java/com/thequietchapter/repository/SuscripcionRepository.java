package com.thequietchapter.repository;

import com.thequietchapter.model.EstadoSuscripcion;
import com.thequietchapter.model.SuscripcionBoletin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<SuscripcionBoletin, Long> {
    Optional<SuscripcionBoletin> findByEmail(String email);

    Optional<SuscripcionBoletin> findByTokenConfirmacion(String token);

    Optional<SuscripcionBoletin> findByTokenCancelacion(String token);

    List<SuscripcionBoletin> findByEstado(EstadoSuscripcion estado);

    Page<SuscripcionBoletin> findByEstado(EstadoSuscripcion estado, Pageable pageable);
}
