package com.thequietchapter.repository;

import com.thequietchapter.model.SolicitudReabastecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudReabastecimientoRepository extends JpaRepository<SolicitudReabastecimiento, Long> {
    List<SolicitudReabastecimiento> findByClienteIdAndActivaTrue(Long clienteId);

    List<SolicitudReabastecimiento> findByLibroIdAndActivaTrue(Long libroId);
}
