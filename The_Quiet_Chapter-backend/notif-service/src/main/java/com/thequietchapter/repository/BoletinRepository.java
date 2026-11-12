package com.thequietchapter.repository;

import com.thequietchapter.model.Boletin;
import com.thequietchapter.model.EstadoBoletin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletinRepository extends JpaRepository<Boletin, Long> {
    Page<Boletin> findByEstado(EstadoBoletin estado, Pageable pageable);
}
