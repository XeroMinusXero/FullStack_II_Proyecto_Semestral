package com.thequietchapter.repository;

import com.thequietchapter.model.EstadoResena;
import com.thequietchapter.model.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByLibroId(Long libroId);

    List<Resena> findByClienteIdOrderByFechaPublicacionDesc(Long clienteId);

    List<Resena> findByEstadoOrderByFechaPublicacionAsc(EstadoResena estado);

    Page<Resena> findByEstado(EstadoResena estado, Pageable pageable);
}
