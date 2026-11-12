package com.thequietchapter.repository;

import com.thequietchapter.model.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedioPagoRepository extends JpaRepository<MedioPago, Long> {
    List<MedioPago> findByActivoTrueOrderByNombreAsc();
}
